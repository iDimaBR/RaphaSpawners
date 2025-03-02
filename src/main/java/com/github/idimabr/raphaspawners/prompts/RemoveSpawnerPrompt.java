package com.github.idimabr.raphaspawners.prompts;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.objects.SpawnerLog;
import com.github.idimabr.raphaspawners.utils.ConfigUtil;
import com.github.idimabr.raphaspawners.utils.NBTAPI;
import com.github.idimabr.raphaspawners.utils.SpawnerUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoveSpawnerPrompt extends NumericPrompt {

    @Override
    public String getPromptText(ConversationContext context) {
        final ConfigUtil messages = RaphaSpawners.getPlugin().getMessages();
        return StringUtils.join(
                messages.getStringList("Prompts.RemoveGenerators")
                        .stream()
                        .map($ -> $.replace("&","§"))
                        .collect(Collectors.toList()), "\n");
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        final ConfigUtil config = RaphaSpawners.getPlugin().getConfiguration();
        final ConfigUtil messages = RaphaSpawners.getPlugin().getMessages();
        final Spawner spawner = (Spawner) context.getSessionData("user");
        final Player player = (Player) context.getForWhom();
        final int amount = input.intValue();

        if(amount <= 0){
            player.sendRawMessage(
                    messages.getString("Errors.NumberInvalid")
                            .replace("&","§")
            );
            return this;
        }

        if(amount == spawner.getQuantity()){
            player.sendRawMessage(messages.getString("Errors.RemoveAllGenerators").replace("&","§"));
            return this;
        }

        if(player.getInventory().firstEmpty() == -1){
            player.sendRawMessage(messages.getString("Errors.DontHaveSpaceInventory").replace("&","§"));
            return this;
        }

        int freeSpace = getSpaceFreeInventory(player);

        if(amount > freeSpace){
            player.sendRawMessage(messages.getString("Errors.DontHaveSpaceInventory").replace("&","§"));
            return this;
        }

        if(amount > spawner.getQuantity()){
            player.sendRawMessage(messages.getString("Errors.NoTakeSuficientGenerators").replace("%quantity_generators%", amount+"").replace("&","§"));
            return this;
        }

        for(int i = 0;i < amount;i++){
            ItemStack item = SpawnerUtils.getSpawner(spawner.getType());
            player.getInventory().addItem(item);
        }

        spawner.setQuantity(spawner.getQuantity() - amount);
        player.sendRawMessage(messages.getString("Success.TakeGenerators").replace("&","§").replace("%quantity_generators%", amount+""));

        SpawnerLog log = new SpawnerLog("§eGeradores retirados");
        log.setLore(Arrays.asList(
                "",
                "§7Usuário: §f" + player.getName(),
                "§7Retirou: §f" + amount,
                "§7Data: §f" + log.getDate()
        ));
        spawner.addLog(log);

        Bukkit.getScheduler().runTask(RaphaSpawners.getPlugin(), spawner::updateHologram);
        return END_OF_CONVERSATION;
    }

    private int getSpaceFreeInventory(Player player) {
        int count = 0;
        for (ItemStack i : player.getInventory()) {
            if (i == null) {
                count += 64;
            }
        }
        return count;
    }
}