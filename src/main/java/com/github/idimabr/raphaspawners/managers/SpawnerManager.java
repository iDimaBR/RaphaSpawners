package com.github.idimabr.raphaspawners.managers;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.MobType;
import com.github.idimabr.raphaspawners.objects.PermissionType;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.objects.SpawnerLog;
import com.github.idimabr.raphaspawners.utils.ItemBuilder;
import com.github.idimabr.raphaspawners.utils.NBTAPI;
import com.github.idimabr.raphaspawners.utils.NumberUtil;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class SpawnerManager {

    private RaphaSpawners plugin;

    public SpawnerManager(RaphaSpawners plugin) {
        this.plugin = plugin;
    }

    public void openPanel(Player player, Spawner spawner){
        if(spawner == null) return;

        Inventory inventory = Bukkit.createInventory(null,
                plugin.getConfigMenu().getInt("Default.SizeRow") * 9,
                plugin.getConfigMenu().getString("Default.Title").replace("&","§")
        );

        if(inventory == null){
            player.sendMessage("§cConfiguração incorreta, chame um administrador.");
            return;
        }

        setItemPanel(inventory, spawner);
        player.openInventory(inventory);
    }

    public void setItemPanel(Inventory inventory, Spawner spawner){
        OfflinePlayer owner = Bukkit.getOfflinePlayer(spawner.getOwner());

        ConfigurationSection section = plugin.getConfigMenu().getConfigurationSection("Default.Items");

        for(String key : section.getKeys(false)){
            ConfigurationSection items = section.getConfigurationSection(key);

            if(!items.contains("Material")) continue;
            if(!items.contains("Slot")) continue;
            if(!items.contains("Title")) continue;

            Material material = Material.getMaterial(items.getString("Material"));
            if(material == null)
                material = Material.EMERALD;

            int slot = items.getInt("Slot");
            String title = items.getString("Title").replace("&","§");

            ItemBuilder builder = new ItemBuilder(material).setName(title);

            if(items.contains("Lore")) {
                List<String> lore = new ArrayList<>();
                for (String s : items.getStringList("Lore")) {
                    lore.add(
                            s.replace("&","§")
                                    .replace("%owner%", owner.getName())
                                    .replace("%status%", (owner.isOnline() ? "§aOnline" : "§cOffline"))
                                    .replace("%spawner_type%", spawner.getEntityName())
                                    .replace("%spawner_quantity%", spawner.getQuantity()+"")
                                    .replace("%spawner_status%", (spawner.getStatus() ? "§aAtivado" : "§cDesativado"))

                    );
                }
                builder.setLore(lore);
            }

            if(items.contains("SkullNick"))
                builder.setSkullOwner(items.getString("SkullNick").replace("%owner%", owner.getName()));

            if(items.contains("Data"))
                builder.setDurability((short) items.getInt("Data"));

            if(items.contains("Glow"))
                builder.setGlow(items.getBoolean("Glow"));

            if(material.toString().contains("SKULL"))
                builder.setDurability((short) 3);

            ItemStack item = builder.toItemStack();
            item = NBTAPI.setNBTData(item, "action", key);

            inventory.setItem(slot, item);
        }
    }

    public void openMenuPermissions(Player player, Spawner spawner){
        if(spawner == null) return;

        Inventory inventory = Bukkit.createInventory(null,
                plugin.getConfigMenu().getInt("Permissions.SizeRow") * 9,
                plugin.getConfigMenu().getString("Permissions.Title").replace("&","§")
        );

        Integer[] slots = plugin.getConfigMenu().getIntegerList("Permissions.Slots").toArray(new Integer[0]);

        int i = 0;
        for (Map.Entry<UUID, List<PermissionType>> entry : spawner.getMembers().entrySet()) {
            if(player.getUniqueId().equals(entry.getKey())) continue;

            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM).setDurability((short) 3);

            builder.setName(plugin.getConfigMenu().getString("Permissions.ItemStack.Member.Name").replace("&","§").replace("%member%", Bukkit.getOfflinePlayer(entry.getKey()).getName()));

            List<String> lore = new ArrayList<>();
            for(String s : plugin.getConfigMenu().getStringList("Permissions.ItemStack.Member.Lore")){
                lore.add(s.replace("&","§"));
            }

            builder.setLore(lore);

            ItemStack item = builder.toItemStack();
            item = NBTAPI.setNBTData(item, "member", entry.getKey().toString());

            inventory.setItem((i < slots.length ? slots[i] : i), item);
            i++;
        }

        inventory.setItem(48, new ItemBuilder(Material.ARROW).setName("§cVoltar").toItemStack());

        Material materialMember = Material.getMaterial(plugin.getConfigMenu().getString("Permissions.ItemStack.AddMember.Material"));
        if(materialMember == null)
            materialMember = Material.SKULL_ITEM;

        ItemBuilder builder = new ItemBuilder(materialMember);

        builder.setName(plugin.getConfigMenu().getString("Permissions.ItemStack.AddMember.Name").replace("&","§"));

        List<String> lore = new ArrayList<>();
        for(String s : plugin.getConfigMenu().getStringList("Permissions.ItemStack.AddMember.Lore")){
            lore.add(s.replace("&","§"));
        }

        builder.setLore(lore);

        inventory.setItem(50, builder.toItemStack());

        player.openInventory(inventory);
    }

    public void openPermissionsPlayer(Player player, OfflinePlayer target, Spawner spawner){

        Inventory inventory = Bukkit.createInventory(null,
                plugin.getConfigMenu().getInt("PermissionsMember.SizeRow") * 9,
                plugin.getConfigMenu().getString("PermissionsMember.Title").replace("%member%", target.getName()).replace("&","§")
        );

        if(!spawner.getMembers().containsKey(target.getUniqueId())){
            player.sendMessage("§cEsse jogador não é um membro do gerador.");
            return;
        }

        setPermissionPanel(target, spawner, inventory);
        player.openInventory(inventory);
    }

    public void setPermissionPanel(OfflinePlayer target, Spawner spawner, Inventory inventory){
        List<PermissionType> permissions = spawner.getMembers().get(target.getUniqueId());

        for (PermissionType permission : PermissionType.values()) {

            Material withPermission = Material.getMaterial(plugin.getConfigMenu().getString("PermissionsMember.WithPermission.Material"));
            Material noPermission = Material.getMaterial(plugin.getConfigMenu().getString("PermissionsMember.NotPermission.Material"));
            if(withPermission == null)
                withPermission = Material.SLIME_BALL;

            if(noPermission == null)
                noPermission = Material.BARRIER;

            int slot = plugin.getConfigMenu().getInt("PermissionsMember.PermissionsType." + permission.name() + ".Slot");
            String name = plugin.getConfigMenu().getString("PermissionsMember.PermissionsType." + permission.name() + ".Name")
                    .replace("%permission%", permission.getName())
                    .replace("&","§");

            ItemBuilder builder = new ItemBuilder(permissions.contains(permission) ? withPermission : noPermission).setName(name);

            
            builder.setDurability((short) plugin.getConfigMenu().getInt("PermissionsMember." + (permissions.contains(permission) ? "WithPermission" : "NotPermission") + ".Data"));


            List<String> lore = new ArrayList<>();
            for(String s : plugin.getConfigMenu().getStringList("PermissionsMember.PermissionsType.Lore")){
                lore.add(s.replace("&","§").replace("%permission_status%", (permissions.contains(permission) ? "§aPermitido" : "§cNão Permitido")));
            }
            builder.setLore(lore);

            ItemStack item = builder.toItemStack();
            item = NBTAPI.setNBTData(item, "hasPermission", String.valueOf( permissions.contains(permission) ));
            item = NBTAPI.setNBTData(item, "permissionUUID", target.getUniqueId().toString());
            item = NBTAPI.setNBTData(item, "permissionID", permission.getID()+"");

            inventory.setItem(slot, item);
        }


        int backSlot = plugin.getConfigMenu().getInt("PermissionsMember.BackSlot");
        inventory.setItem(backSlot, new ItemBuilder(Material.ARROW).setName("§cVoltar").toItemStack());
    }

    // logs

    public void openLogsPanel(Player player, Spawner spawner){
        if(spawner == null) return;

        Inventory inventory = Bukkit.createInventory(null,
                plugin.getConfigMenu().getInt("Logs.SizeRow") * 9,
                plugin.getConfigMenu().getString("Logs.Title").replace("&","§")
        );

        int i = 0;
        for (SpawnerLog spawnerLog : spawner.getLogs()) {
            inventory.setItem(i,
                    new ItemBuilder(Material.PAPER)
                            .setName(spawnerLog.getTitle())
                            .setLore(spawnerLog.getLore())
                            .toItemStack());
            i++;
        }

        inventory.setItem(48, new ItemBuilder(Material.ARROW).setName("§cVoltar").toItemStack());
        inventory.setItem(50, new ItemBuilder(Material.REDSTONE).setName("§cLimpar logs").toItemStack());
        player.openInventory(inventory);
    }

    public void setAI(Entity bukkitEntity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        nmsEntity.c(tag);
        tag.setInt("NoAI", 0);
        nmsEntity.f(tag);
    }

    public void openTopGeneratorPanel(Player player, Spawner spawner) {
        if(spawner == null) return;
        Inventory inventory = Bukkit.createInventory(null,
                plugin.getConfigMenu().getInt("TopGenerator.SizeRow") * 9,
                plugin.getConfigMenu().getString("TopGenerator.Title").replace("&","§")
        );

        Integer[] slotsTop = plugin.getConfigMenu().getIntegerList("TopGenerator.Slots").toArray(new Integer[0]);

        int maxTop = plugin.getConfigMenu().getInt("TopGenerator.NumberOfTOPS");

        for(int i = 0;i < maxTop;i++) {
            if (i < spawner.getTopSpawners().size()) {
                Map.Entry<String, Integer> spawners = spawner.getTopSpawners().get(i);

                int quantityGenerators = spawners.getValue();

                double valuePrice;
                try {
                    valuePrice = plugin.getConfigEntities().getDouble(spawner.getType().name() + ".Price") * quantityGenerators;
                } catch (Exception ex){
                    System.out.println("Ocorreu um erro: " + ex.getLocalizedMessage());
                    valuePrice = 0;
                }

                List<String> lore = Lists.newArrayList();
                for(String s : plugin.getConfigMenu().getStringList("TopGenerator.ItemStack.Lore")){
                    lore.add(s.replace("&","§")
                            .replace("%quantity_generators%", quantityGenerators+"")
                            .replace("%player%", spawners.getKey())
                            .replace("%price_generators%", NumberUtil.formatValue(valuePrice))
                    );
                }

                inventory.setItem(slotsTop[i],
                        new ItemBuilder(Material.SKULL_ITEM)
                                .setDurability((short) 3)
                                .setName(
                                        plugin.getConfigMenu().getString("TopGenerator.ItemStack.Name")
                                        .replace("&","§")
                                                .replace("%player%", spawners.getKey())
                                )
                                .setSkullOwner(spawners.getKey())
                                .setLore(lore)
                                .toItemStack()
                );
            }else{

                inventory.setItem(slotsTop[i],
                        new ItemBuilder(Material.BARRIER)
                                .setDurability((short) 3)
                                .setName("§cNenhum")
                                .toItemStack()
                );
            }
        }

        inventory.setItem(plugin.getConfigMenu().getInt("TopGenerator.BackItemSlot"), new ItemBuilder(Material.ARROW).setName("§cVoltar").toItemStack());
        player.openInventory(inventory);
    }

    public void remasterEntities(){
        int fixed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if(!MobType.hasExists(entity.getType().name())) continue;
                if(entity.getType() == EntityType.ARMOR_STAND) continue;

                if(entity.getCustomName() != null && entity.getCustomName().startsWith("§ex")){
                    int amount = Integer.parseInt(entity.getCustomName().split("§ex")[1].split(" ")[0]);
                    entity.setCustomName("§ex" + amount + " " + MobType.valueOf(entity.getType().name()));
                    entity.setMetadata("stacked", new FixedMetadataValue(RaphaSpawners.getPlugin(), amount));
                    fixed = fixed + amount;
                }
            }
        }
    }
}
