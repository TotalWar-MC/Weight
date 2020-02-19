package com.steffbeard.totalwar.core.mechanics.armor.configuration;

import java.io.OutputStream;
import java.io.FileOutputStream;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.steffbeard.totalwar.core.mechanics.armor.configuration.ConfigurationFile;

import java.io.ByteArrayOutputStream;

public abstract class ResourcedConfigurationFile extends ConfigurationFile
{
    private final ByteArrayOutputStream resourceBytes;
    
    public ResourcedConfigurationFile(final Plugin plugin, final String file, final String resource) {
        this(plugin, file, resource, ConfigurationFile.defaultConfiguration());
    }
    
    public ResourcedConfigurationFile(final Plugin plugin, final String file, final String resource, final FileConfiguration config) {
        this(new File(plugin.getDataFolder(), file), plugin.getResource(resource), config);
    }
    
    public ResourcedConfigurationFile(final File file, final InputStream resource) {
        this(file, resource, ConfigurationFile.defaultConfiguration());
    }
    
    public ResourcedConfigurationFile(final File file, final InputStream resource, final FileConfiguration config) {
        super(file, config);
        this.resourceBytes = new ByteArrayOutputStream();
        try {
            final byte[] buf = new byte[1024];
            int len;
            while ((len = resource.read(buf)) > 0) {
                this.resourceBytes.write(buf, 0, len);
            }
            this.resourceBytes.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public final InputStream getResource() {
        return new ByteArrayInputStream(this.resourceBytes.toByteArray());
    }
    
    @Override
    public void load() throws IOException, InvalidConfigurationException {
        if (!this.getFile().exists()) {
            this.create();
            this.onFileCreate();
        }
        super.load();
    }
    
    protected void onFileCreate() {
    }
    
    public void create() throws IOException {
        this.getFile().getParentFile().mkdirs();
        final InputStream in = this.getResource();
        final OutputStream out = new FileOutputStream(this.getFile());
        final byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    
    public void reset() throws IOException, InvalidConfigurationException {
        this.getFile().delete();
        this.load();
    }
}
