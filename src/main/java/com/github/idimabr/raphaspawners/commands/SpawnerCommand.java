package com.github.idimabr.raphaspawners.commands;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.MobType;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.utils.NBTAPI;
import com.github.idimabr.raphaspawners.utils.SpawnerUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SpawnerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if(!sender.hasPermission("raphaspawners.admin")){
            sender.sendMessage("§cSem permissão!");
            return false;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
            RaphaSpawners.getPlugin().getConfiguration().saveConfig();
            RaphaSpawners.getPlugin().getConfigMenu().saveConfig();
            RaphaSpawners.getPlugin().getConfigEntities().saveConfig();
            RaphaSpawners.getPlugin().getMessages().saveConfig();

            for (Spawner spawner : RaphaSpawners.getSpawners().values())
                spawner.updateHologram();

            sender.sendMessage("§aConfiguração reiniciada!");
            return false;
        }


        if(args.length == 4 && args[0].equalsIgnoreCase("give")){
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if(target == null){
                sender.sendMessage(RaphaSpawners.getPlugin().getMessages().getString("Errors.PlayerNotFound").replace("&","§"));
                return false;
            }

            if(!target.hasPlayedBefore()){
                sender.sendMessage(RaphaSpawners.getPlugin().getMessages().getString("Errors.PlayerNotFound").replace("&","§"));
                return false;
            }

            if(!target.isOnline()){
                sender.sendMessage(RaphaSpawners.getPlugin().getMessages().getString("Errors.PlayerNotFound").replace("&","§"));
                return false;
            }

            Player targetPlayer = target.getPlayer();

            if (!MobType.hasExists(args[2])) {
                sender.sendMessage("§cTipo de monstro não encontrado.");
                sender.sendMessage("§cTipos: §f" + StringUtils.join(Arrays.stream(EntityType.values()).filter(EntityType::isAlive).collect(Collectors.toList()), ", "));
                return false;
            }

            EntityType type = EntityType.valueOf(args[2].toUpperCase());

            int quantidade;

            try {
                quantidade = Integer.parseInt(args[3]);
            }catch(Exception e){
                e.printStackTrace();
                quantidade = 1;
            }

            String entityName = MobType.valueOf(type.name()).getName();

            ItemStack item = SpawnerUtils.getSpawner(type);

            item = NBTAPI.setNBTData(item, "spawnertype", type.name());
            for(int i = 0;i < quantidade;i++)
                targetPlayer.getInventory().addItem(item);

            targetPlayer.sendMessage("§aVocê recebeu x" + quantidade + " Geradores de §f" + entityName);
            sender.sendMessage("§aSpawner enviado para o jogador §f" + target.getName());
            return true;
        }else{
            sender.sendMessage("§cUtilize /spawner give <jogador> <tipo> <quantidade>");
            return false;
        }
    }
}
