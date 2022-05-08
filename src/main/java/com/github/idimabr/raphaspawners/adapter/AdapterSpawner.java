package com.github.idimabr.raphaspawners.adapter;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.PermissionType;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.utils.SerializerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AdapterSpawner {

    public static Spawner toSpawner(ResultSet rs) throws SQLException {
        Location location = SerializerUtils.convertLocation(rs.getString("location"));
        if(location == null) return null;

        if(!location.getChunk().isLoaded())
            location.getChunk().load(true);

        Block block = location.getBlock();
        if(block != null && block.getType() != Material.MOB_SPAWNER)
            location.getBlock().setType(Material.MOB_SPAWNER);

        if(block == null)
            location.getWorld().getBlockAt(location).setType(Material.MOB_SPAWNER);

        EntityType type = EntityType.valueOf(rs.getString("type").toUpperCase());
        UUID owner = UUID.fromString(rs.getString("owner"));

        HashMap<UUID, List<PermissionType>> members = SerializerUtils.convertMembers(rs.getString("members"));
        int quantity = rs.getInt("quantity");

        Spawner spawner = new Spawner(location, type, quantity, owner, members);

        RaphaSpawners.getSpawners().put(location, spawner);
        return spawner;
    }
}
