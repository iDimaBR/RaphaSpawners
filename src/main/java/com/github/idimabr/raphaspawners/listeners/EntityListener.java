package com.github.idimabr.raphaspawners.listeners;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.MobType;
import com.github.idimabr.raphaspawners.objects.Spawner;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
            Collection<Entity> entities = blockLocation.getWorld().getNearbyEntities(blockLocation, radius, radius, radius);

            boolean otherEntity = false;
            if(!entities.isEmpty()) {
                for (Entity entity : entities.stream().filter(en -> en.getType().equals(spawner.getType())).collect(Collectors.toList())) {
                    if(entity.getCustomName() == null) continue;
                    if (entity.getCustomName().isEmpty()) continue;
                    if(!entity.hasMetadata("stacked")) continue;

                    int amount = entity.getMetadata("stacked").get(0).asInt();

                    int limitStack = plugin.getConfiguration().getInt("Entities.LimitStack");

                    if(amount >= limitStack) continue;

                    spawnedEntity.remove();
                    otherEntity = true;

                    int toSpawn;

                    final int random = org.apache.commons.lang3.RandomUtils.nextInt(1, 20);
                    final int random2 = org.apache.commons.lang3.RandomUtils.nextInt(1, 2);

                    int value = (spawner.getQuantity() * random) / 100;
                    if (random2 == 1) {
                        toSpawn = spawner.getQuantity() + value;
                    } else {
                        toSpawn = spawner.getQuantity() - value;
                    }

                    amount = amount + toSpawn;

                    if(amount > limitStack)
                        amount = limitStack;

                    entity.setCustomName("§ex" + amount + " " + spawner.getEntityName());
                    entity.setMetadata("stacked", new FixedMetadataValue(RaphaSpawners.getPlugin(), amount));
                    plugin.getManager().setAI(entity);
                    return;
                }
            }

            if(!otherEntity){
                spawnedEntity.setMetadata("stacked", new FixedMetadataValue(RaphaSpawners.getPlugin(), 1));
                spawnedEntity.setCustomName("§ex1" + " " + spawner.getEntityName());
                plugin.getManager().setAI(spawnedEntity);
            }
        }
    }

    @EventHandler
    public void onDeathMob(EntityDeathEvent e){
        Entity entity = e.getEntity();
        Player player = e.getEntity().getKiller();
        if(entity.getCustomName() == null) return;

        if(entity.hasMetadata("stacked")){
            int amount = entity.getMetadata("stacked").get(0).asInt();

            if(player != null && player.isSneaking()){
                List<ItemStack> drops = new ArrayList<>();
                for (ItemStack drop : e.getDrops()) {
                    drop.setAmount(drop.getAmount() * amount);
                    drops.add(drop);
                }
                e.getDrops().clear();
                drops.forEach(drop -> entity.getWorld().dropItemNaturally(entity.getLocation(), drop));
                player.sendMessage("§aMatou todos de uma vez");
                return;
            }

            if(amount > 1) {
                Entity newEntity = entity.getLocation().getWorld().spawnEntity(entity.getLocation(), entity.getType());
                newEntity.setCustomName("§ex" + (amount - 1) + " " + MobType.valueOf(e.getEntityType().name().toUpperCase()).getName());
                newEntity.setMetadata("stacked", new FixedMetadataValue(RaphaSpawners.getPlugin(), (amount - 1)));
            }
        }
    }
}
