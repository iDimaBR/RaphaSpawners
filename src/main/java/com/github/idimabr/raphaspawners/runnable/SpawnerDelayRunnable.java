package com.github.idimabr.raphaspawners.runnable;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.Spawner;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerDelayRunnable extends BukkitRunnable {
    @Override
    public void run() {
        for (Spawner value : RaphaSpawners.getSpawners().values()) {
            Block block = value.getLocation().getBlock();
            if(block.getType() != Material.MOB_SPAWNER) continue;

            CreatureSpawner cs = (CreatureSpawner) block.getState();
            if(cs == null) continue;
            if(cs.getDelay() == 0) continue;

            if(value.getDelay() == 0){
                value.setDelay(RaphaSpawners.getPlugin().getConfiguration().getInt("Generator.Delay.Init"));
                cs.setDelay(0);
                continue;
            }

            value.setDelay(value.getDelay() - 1);
        }
    }
}
