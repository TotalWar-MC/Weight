package com.steffbeard.totalwar.core.mechanics.armor;

import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class ArmorWeightPlugin extends JavaPlugin
{
    private Logger log;
    private ArmorWeightConfiguration config;
    private ArmorWeightLanguage lang;
    private ArmorWeightCommands commands;
    private WeightManager weightManager;
    private WeightListener listener;
    
    public void onEnable() {
        this.log = this.getLogger();
        this.weightManager = new WeightManager((Plugin)this, this.log);
        (this.config = new ArmorWeightConfiguration((Plugin)this, "config.yml", "config.yml", this.weightManager)).load();
        (this.lang = new ArmorWeightLanguage(this, "lang.yml", "lang.yml")).loadDefaults();
        this.lang.load();
        this.weightManager.setPortableHorsesEnabled(this.isPortableHorsesFound());
        this.weightManager.initialize();
        this.listener = new WeightListener(this.weightManager);
        this.getServer().getPluginManager().registerEvents((Listener)this.listener, (Plugin)this);
        this.commands = new ArmorWeightCommands(this);
        (new WeightTracker(this, this.weightManager)).register();

        for (final Player player : this.getServer().getOnlinePlayers()) {
            if (this.weightManager.isWorldEnabled(player.getWorld())) {
                this.weightManager.loadPlayer(player);
            }
        }
        this.log.info("Enabled successfully.");
    }
    
    private boolean isPortableHorsesFound() {
        return this.getServer().getPluginManager().getPlugin("PortableHorses") != null;
    }
    
    public void onDisable() {
        HandlerList.unregisterAll((Plugin)this);
        for (final Player player : this.getServer().getOnlinePlayers()) {
            this.weightManager.unloadPlayer(player);
        }
        this.log.info("Disabled successfully.");
    }
    
    public ArmorWeightConfiguration getConfiguration() {
        return this.config;
    }
    
    public WeightManager getWeightManager() {
        return this.weightManager;
    }
    
    public ArmorWeightCommands getCommandExecutor() {
        return this.commands;
    }
    
    public ArmorWeightLanguage getLanguage() {
        return this.lang;
    }
    
    public void reload() {
        this.onDisable();
        this.onEnable();
    }
    
    public String getWebsite() {
        return this.getDescription().getWebsite();
    }
    
    public String getVersion() {
        return this.getDescription().getVersion();
    }
}
