package com.github.idimabr.raphaspawners.objects;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class Spawner {

    private boolean status;
    private Location location;
    private Hologram hologram;
    private EntityType type;
    private int quantity;
    private UUID owner;
    private int delay;
    private HashMap<UUID, List<PermissionType>> members = new HashMap<>();
    private List<SpawnerLog> logs = Lists.newArrayList();

    public Spawner(Location location, EntityType type, int quantity, UUID owner, HashMap<UUID, List<PermissionType>> members, List<SpawnerLog> logs) {
        this.location = location;
        this.status = true;

        Block block = location.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;
        CreatureSpawner cs =(CreatureSpawner) block.getState();
        cs.setSpawnedType(type);
        cs.update();

        this.type = type;
        this.quantity = quantity;
        this.owner = owner;
        this.members = members;
        double height = RaphaSpawners.getPlugin().getConfiguration().getDouble("Generator.Hologram.Height");
        this.delay = RaphaSpawners.getPlugin().getConfiguration().getInt("Generator.Delay.Init");
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, height, .5));
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
        cs.update();

        this.type = type;
        this.quantity = quantity;
        this.owner = owner;
        this.members = members;
        double height = RaphaSpawners.getPlugin().getConfiguration().getDouble("Generator.Hologram.Height");
        this.delay = RaphaSpawners.getPlugin().getConfiguration().getInt("Generator.Delay.Init");
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, height, .5));
        updateHologram();
    }

    public Spawner(Location location, EntityType type, UUID owner, HashMap<UUID, List<PermissionType>> members) {
        this.location = location;
        this.status = true;

        Block block = location.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;
        CreatureSpawner cs = (CreatureSpawner) block.getState();
        cs.setSpawnedType(type);
        cs.update();

        this.type = type;
        this.quantity = 1;
        this.owner = owner;
        this.members = members;
        double height = RaphaSpawners.getPlugin().getConfiguration().getDouble("Generator.Hologram.Height");
        this.delay = RaphaSpawners.getPlugin().getConfiguration().getInt("Generator.Delay.Init");
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, height, .5));
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
        cs.update();

        this.type = type;
        this.quantity = quantity;
        this.owner = owner;
        this.members = new HashMap<>();
        double height = RaphaSpawners.getPlugin().getConfiguration().getDouble("Generator.Hologram.Height");
        this.delay = RaphaSpawners.getPlugin().getConfiguration().getInt("Generator.Delay.Init");
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, height, .5));
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
        cs.update();

        this.type = type;
        this.quantity = 1;
        this.owner = owner;
        this.members = new HashMap<>();
        double height = RaphaSpawners.getPlugin().getConfiguration().getDouble("Generator.Hologram.Height");
        this.delay = RaphaSpawners.getPlugin().getConfiguration().getInt("Generator.Delay.Init");
        this.hologram = HologramsAPI.createHologram(RaphaSpawners.getPlugin(), location.clone().add(.5, height, .5));
        updateHologram();
        RaphaSpawners.getSpawners().put(location, this);
    }

    public void updateHologram(){
        hologram.clearLines();
        if(RaphaSpawners.getPlugin().getConfiguration().getBoolean("Generator.MobHead.Enabled")){
            String URL = RaphaSpawners.getPlugin().getConfigEntities().getString(type.name() + ".HeadURL");
            hologram.appendItemLine(getCustomSkull(URL));
        }
        for (String s : RaphaSpawners.getPlugin().getConfiguration().getStringList("Generator.Hologram.Lines")) {
            hologram.appendTextLine(
                    s.replace("&","§")
                            .replace("%quantity_generators%", quantity+"")
                            .replace("%owner%", Bukkit.getOfflinePlayer(owner).getName())
                            .replace("%spawner_type%", getEntityName())
                            .replace("%spawner_status%", (status ? "§aAtivado" : "§cDesativado"))
            );
        }
    }

    public void hologramDisable(Player player){
        this.hologram.getVisibilityManager().hideTo(player);
    }

    public void hologramEnable(Player player){
        this.hologram.getVisibilityManager().showTo(player);
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
        if(this.hologram == null) return;
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

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public List<Map.Entry<String, Integer>> getTopSpawners(){
        HashMap<String, Integer> tops = Maps.newHashMap();
        for (Map.Entry<Location, Spawner> entry : RaphaSpawners.getSpawners().entrySet()) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(entry.getValue().getOwner());

            Spawner spawner = entry.getValue();

            tops.put(owner.getName(), tops.getOrDefault(owner.getName(), 0) + spawner.getQuantity());
        }

        return tops.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(15).collect(Collectors.toList());
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

    public ItemStack getCustomSkull(String url) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (url == null || url.isEmpty())
            return skull;
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
