package com.steffbeard.totalwar.core.mechanics.armor.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.steffbeard.totalwar.core.mechanics.armor.configuration.ConfigurationFile;

public abstract class UpdatableConfigurationFile extends ResourcedConfigurationFile
{
    private String updateFromVersion;
    private String versionKey;
    
    public UpdatableConfigurationFile(final Plugin plugin, final String file, final String resource) {
        this(plugin, file, resource, ConfigurationFile.defaultConfiguration());
    }
    
    public UpdatableConfigurationFile(final Plugin plugin, final String file, final String resource, final FileConfiguration config) {
        this(new File(plugin.getDataFolder(), file), plugin.getResource(resource), config);
    }
    
    public UpdatableConfigurationFile(final File file, final InputStream resource) {
        this(file, resource, ConfigurationFile.defaultConfiguration());
    }
    
    public UpdatableConfigurationFile(final File file, final InputStream resource, final FileConfiguration config) {
        super(file, resource, config);
        this.updateFromVersion = null;
        this.versionKey = defaultVersionKey();
    }
    
    protected void update(final FileConfiguration config) {
    }
    
    protected void updateStart(final String fromVersion) {
        if (this.updateFromVersion == null) {
            this.onUpdateStart(this.updateFromVersion = fromVersion);
        }
    }
    
    protected void onUpdateStart(final String fromVersion) {
    }
    
    protected void updateDone(final String toVersion) throws IOException {
        if (this.updateFromVersion != null) {
            this.getConfig().set(this.versionKey, (Object)toVersion);
            this.save();
            this.onUpdateDone(this.updateFromVersion, toVersion);
            this.updateFromVersion = null;
        }
    }
    
    protected void onUpdateDone(final String fromVersion, final String toVersion) {
    }
    
    public String getVersion() {
        return this.getConfig().getString(this.versionKey, "unknown");
    }
    
    protected String getVersionKey() {
        return this.versionKey;
    }
    
    protected void setVersionKey(final String key) {
        this.versionKey = key;
    }
    
    public static String defaultVersionKey() {
        return "config.version";
    }
}
