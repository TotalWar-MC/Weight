package com.steffbeard.totalwar.core.mechanics.armor.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.IOException;
import java.util.Iterator;
import java.io.BufferedReader;
import java.util.Map;
import java.io.Reader;
import java.nio.charset.Charset;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class ConfigurationFile
{
    private final FileConfiguration config;
    private final File file;
    private Charset charset;
    
    public ConfigurationFile(final File file) {
        this(file, defaultConfiguration());
    }
    
    public ConfigurationFile(final File file, final FileConfiguration config) {
        this.file = file;
        this.config = config;
        this.charset = defaultCharset();
    }
    
    public final FileConfiguration getConfig() {
        return this.config;
    }
    
    public final File getFile() {
        return this.file;
    }
    
    public String getFileName() {
        return this.file.getName();
    }
    
    public Charset getCharset() {
        return this.charset;
    }
    
    public void setCharset(final Charset charset) {
        this.charset = charset;
    }
    
    public void setCharset(final String charsetName) {
        this.setCharset(Charset.forName(charsetName));
    }
    
    public static FileConfiguration loadConfiguration(final FileConfiguration config, final Reader reader) throws IOException, InvalidConfigurationException {
        for (final Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            config.set((String)entry.getKey(), (Object)null);
        }
        final BufferedReader input = (BufferedReader)((reader instanceof BufferedReader) ? reader : new BufferedReader(reader));
        final StringBuilder builder = new StringBuilder();
        try {
            String line;
            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        }
        finally {
            input.close();
        }
        config.loadFromString(builder.toString());
        return config;
    }
    
    public static FileConfiguration loadConfiguration(final FileConfiguration config, final File file, final Charset charset) throws FileNotFoundException, IOException, InvalidConfigurationException {
        return loadConfiguration(config, new InputStreamReader(new FileInputStream(file), charset));
    }
    
    public static FileConfiguration loadConfiguration(final FileConfiguration config, final InputStream input, final Charset charset) throws IOException, InvalidConfigurationException {
        return loadConfiguration(config, new InputStreamReader(input, charset));
    }
    
    public void load() throws FileNotFoundException, IOException, InvalidConfigurationException {
        loadConfiguration(this.config, this.file, this.charset);
        this.loadValues(this.config);
    }
    
    protected abstract void loadValues(final FileConfiguration p0);
    
    public void save() throws IOException {
        final Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file), this.charset));
        fileWriter.write(this.config.saveToString());
        fileWriter.close();
    }
    
    public void move(final File toFile) throws IOException {
        if (toFile.exists()) {
            toFile.delete();
        }
        if (!this.file.renameTo(toFile)) {
            throw new IOException("File not moved!");
        }
    }
    
    public void copy(final File toFile) throws IOException {
        if (toFile.exists()) {
            toFile.delete();
        }
        InputStream is = null;
        OutputStream os = null;
        is = new FileInputStream(this.file);
        os = new FileOutputStream(toFile);
        final byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.close();
    }
    
    protected boolean setIfNotExists(final String path, final Object value) {
        if (!this.config.contains(path)) {
            this.config.set(path, value);
            return true;
        }
        return false;
    }
    
    public static FileConfiguration defaultConfiguration() {
        return (FileConfiguration)new YamlConfiguration();
    }
    
    public static Charset defaultCharset() {
        return Charset.forName("UTF-8");
    }
}
