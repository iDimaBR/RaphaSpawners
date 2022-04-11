package com.github.idimabr.raphaspawners.commands;

import com.github.idimabr.raphaspawners.objects.MobType;
import com.github.idimabr.raphaspawners.utils.ItemBuilder;
import com.github.idimabr.raphaspawners.utils.NBTAPI;
import com.github.idimabr.raphaspawners.utils.EnumUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SpawnerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(args.length == 4 && args[0].equalsIgnoreCase("give")){
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if(target == null){
                sender.sendMessage("§cJogador não encontrado.");
                return false;
            }

            if(!target.hasPlayedBefore()){
                sender.sendMessage("§cJogador não encontrado.");
                return false;
            }

            if(!target.isOnline()){
                sender.sendMessage("§cJogador não encontrado.");
                return false;
            }

            Player targetPlayer = target.getPlayer();

            if (!EnumUtils.isValidEnum(MobType.class, args[2].toUpperCase())) {
                sender.sendMessage("§cTipo de monstro não encontrado.");
                sender.sendMessage("§cTipos: §f" + StringUtils.join(MobType.values(), ", "));
                return false;
            }

            EntityType type = EntityType.valueOf(args[2].toUpperCase());

            int quantidade;

            try {
                quantidade = Integer.parseInt(args[3]);
            }catch(NumberFormatException e){
                quantidade = 1;
            }

            String entityName = MobType.valueOf(type.name()).getName();

            ItemStack item = new ItemBuilder(Material.MOB_SPAWNER, quantidade)
                    .setName("§eGerador de Monstros")
                    .setLore("§7Tipo: §f" + entityName)
                    .toItemStack();

            item = NBTAPI.setNBTData(item, "spawnertype", type.name());

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
