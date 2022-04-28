package com.github.idimabr.raphaspawners.listeners;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.MobType;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.utils.ItemBuilder;
import com.github.idimabr.raphaspawners.utils.SpawnerUtils;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EntityListener implements Listener {

    private RaphaSpawners plugin;

    public EntityListener(RaphaSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void spawnMob(SpawnerSpawnEvent e) {
        CreatureSpawner cs = e.getSpawner();
        if(cs == null) return;
        Location blockLocation = cs.getLocation();

        if(RaphaSpawners.getSpawners().containsKey(blockLocation)){
            Spawner spawner = RaphaSpawners.getSpawners().get(blockLocation);
            if(!spawner.getStatus()){
                e.setCancelled(true);
                return;
            }

            if(plugin.getConfiguration().getStringList("WorldBlackList").contains(blockLocation.getWorld().getName())) return;


            Entity spawnedEntity = e.getEntity();
            int radius = plugin.getConfiguration().getInt("Entities.DistanceStack");
            int limitStack = plugin.getConfiguration().getInt("Entities.LimitStack");

            Collection<Entity> entities = blockLocation.getWorld()
                    .getNearbyEntities(blockLocation, radius, radius, radius)
                    .stream()
                    .filter(en -> en.hasMetadata("stacked") && en.getMetadata("stacked").size() != 0 && en.getMetadata("stacked").get(0).asInt() < limitStack)
                    .collect(Collectors.toList());

            SpawnerUtils.disableAI(e.getEntity());

            if(!entities.isEmpty()) {
                for (Entity entity : entities) {
                    if(!entity.hasMetadata("stacked")) continue;

                    int amount = entity.getMetadata("stacked").get(0).asInt();

                    if(amount >= limitStack) continue;

                    spawnedEntity.remove();

                    int toSpawn = 0;

                    if(plugin.getConfiguration().getBoolean("Generator.RandomValueSpawn")) {

                        final int random = org.apache.commons.lang3.RandomUtils.nextInt(1, 20);
                        final int random2 = org.apache.commons.lang3.RandomUtils.nextInt(1, 2);

                        int value = (spawner.getQuantity() * random) / 100;
                        if (random2 == 1) {
                            toSpawn = amount + value;
                        } else {
                            toSpawn = amount - value;
                        }
                    }

                    toSpawn = spawner.getQuantity() + amount;

                    if(toSpawn > limitStack)
                        toSpawn = limitStack;


                    entity.setCustomName("§ex" + toSpawn + " " + spawner.getEntityName());
                    entity.setMetadata("stacked", new FixedMetadataValue(RaphaSpawners.getPlugin(), toSpawn));
                    return;
                }
            }else{
                if(spawnedEntity.hasMetadata("stacked")) return;

                int amount = spawner.getQuantity();

                if(amount > limitStack)
                    amount = limitStack;

                spawnedEntity.setMetadata("stacked", new FixedMetadataValue(RaphaSpawners.getPlugin(), amount));
                spawnedEntity.setCustomName("§ex" + amount + " " + spawner.getEntityName());
            }
        }
    }

    @EventHandler
    public void onDeathMob2(EntityDamageByEntityEvent e){
        if(e.getDamager() == null) return;
        if(e.getDamager().getType() != EntityType.PLAYER) return;

        Player player = (Player) e.getDamager();

        if(e.getEntity().isDead() || !e.getEntity().isValid()) return;

        LivingEntity entity = (LivingEntity) e.getEntity();
        if(!entity.hasMetadata("stacked")) return;
        if(entity.getMetadata("stacked").size() == 0) return;

        if(entity.getHealth() <= e.getDamage()){

            e.setCancelled(true);

            entity.setHealth(entity.getMaxHealth());

            int amount = entity.getMetadata("stacked").get(0).asInt();

            sendCommands(entity.getType(), player);

            if(amount > 1 && player.isSneaking()){
                entity.setCustomName("§ex" + (amount - 1) + " " + MobType.valueOf(e.getEntityType().name().toUpperCase()).getName());
                entity.setMetadata("stacked", new FixedMetadataValue(RaphaSpawners.getPlugin(), (amount - 1)));
                sendDrops(entity.getLocation(), entity.getType(), player, 1);
            }else{
                sendDrops(entity.getLocation(), entity.getType(), player, amount);
                entity.remove();
            }
        }
    }

    //@EventHandler
    public void onDeathMob(EntityDeathEvent e){
        Entity entity = e.getEntity();
        Player player = e.getEntity().getKiller();
        if(entity.getCustomName() == null) return;

        if(entity.hasMetadata("stacked")){
            e.getDrops().clear();

            int amount = entity.getMetadata("stacked").get(0).asInt();
            sendCommands(entity.getType(), player);

            if(amount > 1 && player.isSneaking()) {
                Entity newEntity = entity.getLocation().getWorld().spawnEntity(entity.getLocation(), entity.getType());
                newEntity.setCustomName("§ex" + (amount - 1) + " " + MobType.valueOf(e.getEntityType().name().toUpperCase()).getName());
                newEntity.setMetadata("stacked", new FixedMetadataValue(RaphaSpawners.getPlugin(), (amount - 1)));
                SpawnerUtils.disableAI(newEntity);
                sendDrops(entity.getLocation(), entity.getType(), player, 1);
                return;
            }

            sendDrops(entity.getLocation(), entity.getType(), player, amount);
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
        }
    }

    public void sendDrops(Location location, EntityType type, Player player, int stacked){
        ConfigurationSection section = plugin.getConfigEntities().getConfigurationSection(type.name() + ".Drops");
        if(section == null) return;

        ItemStack itemHand = player.getItemInHand();

        for(String key : section.getKeys(false)) {
            ConfigurationSection items = section.getConfigurationSection(key);

            if (!items.contains("Material")) continue;

            Material material = Material.getMaterial(items.getString("Material"));
            if(material == null){
                System.out.println("Material do drop é nulo, pulando...");
                continue;
            }
            
            int amount = 1;
            if(items.contains("Amount"))
                amount = items.getInt("Amount");

            amount = amount * stacked;

            ItemBuilder builder = new ItemBuilder(material, amount);

            if(items.contains("Name"))
                builder.setName(items.getString("Name").replace("&", "§"));

            if (items.contains("Lore")) {
                List<String> lore = new ArrayList<>();
                for (String s : items.getStringList("Lore")) {
                    lore.add(s.replace("&", "§"));
                }
                builder.setLore(lore);
            }

            if (items.contains("Data"))
                builder.setDurability((short) items.getInt("Data"));

            if (items.contains("Glow"))
                builder.setGlow(items.getBoolean("Glow"));

            if(items.contains("ChanceDrop"))
                if(RandomUtils.nextDouble(0, 100) > items.getDouble("ChanceDrop")) continue;

            if(items.contains("Looting"))
                if(items.getBoolean("Looting") && itemHand.getEnchantments().containsKey(Enchantment.LOOT_BONUS_MOBS)){
                    int levelEnchant = itemHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
                    amount = amount * levelEnchant;
                    builder.setAmount(amount);
                }

            location.getWorld().dropItemNaturally(location, builder.toItemStack());
        }
    }

    private void sendCommands(EntityType type, Player player){
        double chance = plugin.getConfigEntities().getDouble(type.name() + ".Rewards.Chance");
        if(RandomUtils.nextDouble(0, 100) < chance)
            for (String s : plugin.getConfigEntities().getStringList(type.name() + ".Rewards.Commands")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName()).replace("&","§"));
            }
    }
}
