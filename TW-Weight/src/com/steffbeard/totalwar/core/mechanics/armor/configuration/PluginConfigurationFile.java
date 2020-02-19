package com.steffbeard.totalwar.core.mechanics.armor.configuration;

import org.bukkit.Bukkit;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.steffbeard.totalwar.core.mechanics.armor.configuration.ConfigurationFile;

import java.util.logging.Logger;

public abstract class PluginConfigurationFile extends UpdatableConfigurationFile
{
    private static Logger log;
    private boolean updateSilently;
    
    public PluginConfigurationFile(final Plugin plugin, final String file, final String resource) {
        this(plugin, file, resource, ConfigurationFile.defaultConfiguration());
    }
    
    public PluginConfigurationFile(final Plugin plugin, final String file, final String resource, final FileConfiguration config) {
        this(new File(plugin.getDataFolder(), file), plugin.getResource(resource), config);
    }
    
    public PluginConfigurationFile(final File file, final InputStream resource) {
        this(file, resource, ConfigurationFile.defaultConfiguration());
    }
    
    public PluginConfigurationFile(final File file, final InputStream resource, final FileConfiguration config) {
        super(file, resource, config);
        this.updateSilently = false;
    }
    
    public static FileConfiguration loadConfiguration(final FileConfiguration config, final Reader reader) {
        try {
            return ConfigurationFile.loadConfiguration(config, reader);
        }
        catch (IOException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file using reader:", new Object[0]));
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e2) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file using reader. Configuration invalid:", new Object[0]));
            e2.printStackTrace();
        }
        return config;
    }
    
    public static FileConfiguration loadConfiguration(final FileConfiguration config, final File file, final Charset charset) {
        try {
            return ConfigurationFile.loadConfiguration(config, file, charset);
        }
        catch (FileNotFoundException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file using reader. File not found:", new Object[0]));
            e.printStackTrace();
        }
        catch (IOException e2) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file %s:", file.getName()));
            e2.printStackTrace();
        }
        catch (InvalidConfigurationException e3) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file %s. Configuration invalid:", file.getName()));
            e3.printStackTrace();
        }
        return config;
    }
    
    public static FileConfiguration loadConfiguration(final FileConfiguration config, final InputStream input, final Charset charset) {
        try {
            return ConfigurationFile.loadConfiguration(config, input, charset);
        }
        catch (IOException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file using stream:", new Object[0]));
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e2) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file using stream. Configuration invalid:", new Object[0]));
            e2.printStackTrace();
        }
        return config;
    }
    
    @Override
    public void load() {
        try {
            super.load();
        }
        catch (FileNotFoundException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file %s! File not found:", this.getFileName()));
            e.printStackTrace();
        }
        catch (IOException e2) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file %s:", this.getFileName()));
            e2.printStackTrace();
        }
        catch (InvalidConfigurationException e3) {
            PluginConfigurationFile.log.warning(String.format("Failed to load configuration file %s! The configuration is invalid:", this.getFileName()));
            e3.printStackTrace();
        }
    }
    
    @Override
    public void save() {
        try {
            super.save();
        }
        catch (IOException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to save configuration file %s:", this.getFileName()));
            e.printStackTrace();
        }
    }
    
    @Override
    public void create() {
        try {
            super.create();
        }
        catch (IOException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to create configuration file %s:", this.getFileName()));
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onFileCreate() {
        PluginConfigurationFile.log.info(String.format("Created new configuration file %s from defaults.", this.getFileName()));
    }
    
    @Override
    public void reset() {
        try {
            super.reset();
        }
        catch (IOException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to reset configuration file %s:", this.getFileName()));
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e2) {
            PluginConfigurationFile.log.warning(String.format("Failed to reset configuration file %s! The configuration is invalid:", this.getFileName()));
            e2.printStackTrace();
        }
    }
    
    @Override
    public void move(final File newFile) {
        try {
            super.move(newFile);
        }
        catch (IOException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to move file %s to %s:", this.getFileName(), newFile.getName()));
            e.printStackTrace();
        }
    }
    
    @Override
    public void copy(final File newFile) {
        try {
            super.copy(newFile);
        }
        catch (IOException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to copy file %s to %s:", this.getFileName(), newFile.getName()));
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onUpdateStart(final String startVersion) {
        if (!this.updateSilently) {
            PluginConfigurationFile.log.warning(String.format("Configuration file %s is outdatet (v%s)!", this.getFileName(), startVersion));
            PluginConfigurationFile.log.info(String.format("Attempting to update %s ...", this.getFileName()));
        }
    }
    
    @Override
    protected void updateDone(final String toVersion) {
        try {
            super.updateDone(toVersion);
        }
        catch (IOException e) {
            PluginConfigurationFile.log.warning(String.format("Failed to save updated configuration file %s to version %s:", this.getFileName(), toVersion));
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onUpdateDone(final String startVersion, final String endVersion) {
        if (!this.updateSilently) {
            PluginConfigurationFile.log.info(String.format("Configuration file %s has been updated automatically from v%s to v%s!", this.getFileName(), startVersion, endVersion));
            PluginConfigurationFile.log.info(String.format("Please check if the update was successful.", new Object[0]));
        }
    }
    
    public static void setLogger(final Logger logger) {
        PluginConfigurationFile.log = logger;
    }
    
    protected void setUpdateSilently(final boolean updateSilently) {
        this.updateSilently = updateSilently;
    }
    
    public boolean isUpdateSilently() {
        return this.updateSilently;
    }
    
    static {
        PluginConfigurationFile.log = Bukkit.getLogger();
    }
}
