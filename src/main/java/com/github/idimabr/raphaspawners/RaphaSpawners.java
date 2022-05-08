package com.github.idimabr.raphaspawners;

import com.github.idimabr.raphaspawners.commands.SpawnerCommand;
import com.github.idimabr.raphaspawners.listeners.EntityListener;
import com.github.idimabr.raphaspawners.listeners.SpawnerListener;
import com.github.idimabr.raphaspawners.managers.SpawnerManager;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.runnable.SpawnerDelayRunnable;
import com.github.idimabr.raphaspawners.storage.CacheSQL;
import com.github.idimabr.raphaspawners.storage.MySQL;
import com.github.idimabr.raphaspawners.utils.ConfigUtil;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class RaphaSpawners extends JavaPlugin {

    private static RaphaSpawners plugin;
    private ConfigUtil config;
    private ConfigUtil configMenu;
    private ConfigUtil configEntities;
    private ConfigUtil messages;
    private MySQL SQL;
    private static HashMap<Location, Spawner> SPAWNERS = Maps.newHashMap();
    private SpawnerManager manager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }

        plugin = this;
        config = new ConfigUtil(null, "config.yml", false);
        configMenu = new ConfigUtil(null, "menus.yml", false);
        configEntities = new ConfigUtil(null, "entities.yml", false);
        messages = new ConfigUtil(null, "messages.yml", false);
        SQL = new MySQL(this);
        SQL.createTable();

        config.saveConfig();
        configMenu.saveConfig();
        messages.saveConfig();

        manager = new SpawnerManager(this);
        CacheSQL.loadCache();

        getCommand("spawner").setExecutor(new SpawnerCommand());
        Bukkit.getPluginManager().registerEvents(new SpawnerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(this), this);
        //Bukkit.getPluginManager().registerEvents(new ItemStackListener(), this);
        manager.remasterEntities();
        new SpawnerDelayRunnable().runTaskTimer(this, 20L, 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        messages.reloadConfig();
        config.reloadConfig();
        configMenu.reloadConfig();
        CacheSQL.saveCache();
        manager.remasterEntities();
    }

    public static RaphaSpawners getPlugin() {
        return plugin;
    }

    public static HashMap<Location, Spawner> getSpawners() {
        return SPAWNERS;
    }

    public SpawnerManager getManager() {
        return manager;
    }

    public ConfigUtil getConfiguration() {
        return config;
    }

    public ConfigUtil getConfigMenu() {
        return configMenu;
    }

    public ConfigUtil getConfigEntities() {
        return configEntities;
    }

    public ConfigUtil getMessages() {
        return messages;
    }

    public MySQL getSQL() {
        return SQL;
    }
}
