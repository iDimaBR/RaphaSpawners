package com.github.idimabr.raphaspawners.objects;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import java.util.*;

public class Spawner {

    private boolean status;
    private Location location;
    private Hologram hologram;
    private EntityType type;
    private int quantity;
    private UUID owner;
    private HashMap<UUID, List<PermissionType>> members = new HashMap<>();
    private List<SpawnerLog> logs = Lists.newArrayList();

    public Spawner(Location location, EntityType type, int quantity, UUID owner, HashMap<UUID, List<PermissionType>> members, List<SpawnerLog> logs) {
        this.location = location;
        this.status = true;

        Block block = location.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;
        CreatureSpawner cs =(CreatureSpawner) block.getState();
        cs.setSpawnedType(type);
        cs.update(true);

        this.type = type;
        this.quantity = quantity;
        this.owner = owner;
        this.members = members;
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, 2.5, .5));
        this.logs = logs;
        updateHologram();
    }

    public Spawner(Location location, EntityType type, int quantity, UUID owner, HashMap<UUID, List<PermissionType>> members) {
        this.location = location;
        this.status = true;

        Block block = location.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;
        CreatureSpawner cs =(CreatureSpawner) block.getState();
        cs.setSpawnedType(type);
        cs.update(true);

        this.type = type;
        this.quantity = quantity;
        this.owner = owner;
        this.members = members;
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, 2.5, .5));
        updateHologram();
    }

    public Spawner(Location location, EntityType type, UUID owner, HashMap<UUID, List<PermissionType>> members) {
        this.location = location;
        this.status = true;

        Block block = location.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;
        CreatureSpawner cs = (CreatureSpawner) block.getState();
        cs.setSpawnedType(type);
        cs.update(true);

        this.type = type;
        this.quantity = 1;
        this.owner = owner;
        this.members = members;
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, 2.5, .5));
        updateHologram();
        RaphaSpawners.getSpawners().put(location, this);
    }

    public Spawner(Location location, EntityType type, int quantity, UUID owner) {
        this.location = location;
        this.status = true;

        Block block = location.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;
        CreatureSpawner cs =(CreatureSpawner) block.getState();
        cs.setSpawnedType(type);
        cs.update(true);

        this.type = type;
        this.quantity = quantity;
        this.owner = owner;
        this.members = new HashMap<>();
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, 2.5, .5));
        updateHologram();
        RaphaSpawners.getSpawners().put(location, this);
    }

    public Spawner(Location location, EntityType type, UUID owner) {
        this.location = location;
        this.status = true;

        Block block = location.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;
        CreatureSpawner cs =(CreatureSpawner) block.getState();
        cs.setSpawnedType(type);
        cs.update(true);

        this.type = type;
        this.quantity = 1;
        this.owner = owner;
        this.members = new HashMap<>();
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, 2.5, .5));
        updateHologram();
        RaphaSpawners.getSpawners().put(location, this);
    }

    public void updateHologram(){
        hologram.clearLines();
        hologram.appendTextLine("§ex" + quantity + " Gerador" + (quantity > 1 ? "es" : "") + " de " + getEntityName());
        hologram.appendTextLine("§eProprietário: §f" + Bukkit.getOfflinePlayer(owner).getName());
        hologram.appendTextLine("§eStatus: §f" + (status ? "§aAtivado" : "§cDesativado"));
    }

    public List<SpawnerLog> getLogs() {
        return logs;
    }

    public void clearLogs(){
        this.logs.clear();
    }

    public void addLog(SpawnerLog log){
        if(this.logs.size() > 44) this.logs.clear();
        this.logs.add(log);
    }

    public void setLogs(List<SpawnerLog> logs) {
        this.logs = logs;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public void deleteHologram(){
        this.hologram.delete();
    }

    public void setHologramVisibility(boolean status){
        this.hologram.getVisibilityManager().resetVisibilityAll();
        this.hologram.getVisibilityManager().setVisibleByDefault(status);
    }

    public EntityType getType() {
        return type;
    }

    public String getEntityName(){
        return MobType.valueOf(type.name()).getName();
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void addMemberPermission(UUID uuid, PermissionType permission){
        Set<Map.Entry<UUID, List<PermissionType>>> newEntry = members.entrySet();
        for (Map.Entry<UUID, List<PermissionType>> entry : newEntry) {
            if(!entry.getKey().equals(uuid)) continue;
            members.get(uuid).add(permission);
        }
    }

    public void removeMemberPermission(UUID uuid, PermissionType permission){
        Set<Map.Entry<UUID, List<PermissionType>>> newEntry = members.entrySet();
        for (Map.Entry<UUID, List<PermissionType>> entry : newEntry) {
            if(!entry.getKey().equals(uuid)) continue;
            members.get(uuid).remove(permission);
        }
    }

    public boolean memberHasPermission(PermissionType permission, UUID uuid){
        Set<Map.Entry<UUID, List<PermissionType>>> newEntry = members.entrySet();
        for (Map.Entry<UUID, List<PermissionType>> entry : newEntry) {
            if(!entry.getKey().equals(uuid)) continue;
            if(entry.getValue().contains(permission)) return true;
        }
        return false;
    }

    public HashMap<UUID, List<PermissionType>> getMembers() {
        return members;
    }

    public void setMembers(HashMap<UUID, List<PermissionType>> members) {
        this.members = members;
    }
}
