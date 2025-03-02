package com.github.idimabr.raphaspawners.prompts;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.objects.SpawnerLog;
import com.github.idimabr.raphaspawners.utils.ConfigUtil;
import com.github.idimabr.raphaspawners.utils.NBTAPI;
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

public class AddSpawnerPrompt extends NumericPrompt {

    @Override
    public String getPromptText(ConversationContext context) {
        final ConfigUtil messages = RaphaSpawners.getPlugin().getMessages();
        return StringUtils.join(
                messages.getStringList("Prompts.AddGenerators")
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

        if(amount > 2304){
            player.sendRawMessage(
                    messages.getString("Errors.AmountMoreThanMax")
                            .replace("&","§")
            );
            return this;
        }

        final int limitStack = config.getInt("Generator.LimitStack");
        if(!hasAmountSpawner(player, amount, spawner.getType().name())){
            player.sendRawMessage(
                    messages.getString("Errors.NoSuficientGenerators")
                            .replace("&","§")
            );
            return this;
        }

        if((spawner.getQuantity() + amount) > limitStack){
            player.sendRawMessage(
                    messages.getString("Errors.AddGeneratorsMoreThanLimit")
                            .replace("&","§")
                            .replace("%quantity_generators%", (limitStack - spawner.getQuantity())+"")
                            .replace("%limitstack%", limitStack+"")
            );
            return this;
        }

        removeSpawnersFromPlayer(player, amount, spawner.getType().name());
        spawner.setQuantity(spawner.getQuantity() + amount);
        player.sendRawMessage(
                messages.getString("Success.StoreGenerators")
                        .replace("%quantity_generators%", amount+"")
                        .replace("&","§")
        );

        final SpawnerLog log = new SpawnerLog("§eGeradores adicionados");
        log.setLore(Arrays.asList(
                "",
                "§7Usuário: §f" + player.getName(),
                "§7Adicionou: §f" + amount,
                "§7Data: §f" + log.getDate()
        ));
        spawner.addLog(log);

        Bukkit.getScheduler().runTask(RaphaSpawners.getPlugin(), spawner::updateHologram);
        return END_OF_CONVERSATION;
    }

    private boolean hasAmountSpawner(Player player, int needed, String type){
        PlayerInventory inventory = player.getInventory();
        int amount = 0;
        for (ItemStack itemStack : inventory) {
            if(itemStack != null && itemStack.getType() == Material.MOB_SPAWNER) {
                if(!NBTAPI.hasTag(itemStack, "spawnertype")) continue;
                if(!NBTAPI.getTag(itemStack, "spawnertype").equals(type)) continue;
                amount += itemStack.getAmount();
            }
        }

        return amount >= needed;
    }

    private void removeSpawnersFromPlayer(Player player, int count, String type) {
        Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(Material.MOB_SPAWNER);

        int found = 0;
        for (ItemStack stack : ammo.values()) {
            if(!NBTAPI.hasTag(stack, "spawnertype")) continue;
            if(!NBTAPI.getTag(stack, "spawnertype").equals(type)) continue;
            found += stack.getAmount();
        }
        if (count > found)
            return;

        for (Integer index : ammo.keySet()) {
            ItemStack stack = ammo.get(index);
            if(!NBTAPI.hasTag(stack, "spawnertype")) continue;
            if(!NBTAPI.getTag(stack, "spawnertype").equals(type)) continue;

            int removed = Math.min(count, stack.getAmount());
            count -= removed;

            if (stack.getAmount() == removed)
                player.getInventory().setItem(index, null);
            else
                stack.setAmount(stack.getAmount() - removed);

            if (count <= 0)
                break;
        }

        player.updateInventory();
    }
}