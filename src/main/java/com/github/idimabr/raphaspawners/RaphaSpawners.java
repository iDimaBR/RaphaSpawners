package com.github.idimabr.raphaspawners;

import com.github.idimabr.raphaspawners.commands.SpawnerCommand;
import com.github.idimabr.raphaspawners.listeners.EntityListener;
import com.github.idimabr.raphaspawners.listeners.ItemStackListener;
import com.github.idimabr.raphaspawners.listeners.SpawnerListener;
import com.github.idimabr.raphaspawners.managers.SpawnerManager;
import com.github.idimabr.raphaspawners.objects.Spawner;
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
        SQL = new MySQL(this);
        SQL.createTable();
        manager = new SpawnerManager(this);
        CacheSQL.loadCache();

        getCommand("spawner").setExecutor(new SpawnerCommand());
        Bukkit.getPluginManager().registerEvents(new SpawnerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemStackListener(), this);

        config.saveConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        config.reloadConfig();
        CacheSQL.saveCache();
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

    public MySQL getSQL() {
        return SQL;
    }
}
