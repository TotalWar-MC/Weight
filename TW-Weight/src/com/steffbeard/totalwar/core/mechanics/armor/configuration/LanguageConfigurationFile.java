package com.steffbeard.totalwar.core.mechanics.armor.configuration;

import com.steffbeard.totalwar.core.mechanics.armor.chat.*;
import com.steffbeard.totalwar.core.mechanics.armor.configuration.ConfigurationFile;

import org.bukkit.ChatColor;
import java.io.InputStream;
import java.io.File;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class LanguageConfigurationFile extends PluginConfigurationFile
{
    private final MessageValueMap formatOptions;
    private final FileConfiguration defaultConfiguration;
    
    public LanguageConfigurationFile(final Plugin plugin, final String file, final String resource) {
        this(plugin, file, resource, ConfigurationFile.defaultConfiguration());
    }
    
    public LanguageConfigurationFile(final Plugin plugin, final String file, final String resource, final FileConfiguration config) {
        this(new File(plugin.getDataFolder(), file), plugin.getResource(resource), config);
    }
    
    public LanguageConfigurationFile(final File file, final InputStream resource) {
        this(file, resource, ConfigurationFile.defaultConfiguration());
    }
    
    public LanguageConfigurationFile(final File file, final InputStream resource, final FileConfiguration config) {
        super(file, resource, config);
        (this.formatOptions = new MessageValueMap()).put("aqua", ChatColor.AQUA.toString());
        this.formatOptions.put("black", ChatColor.BLACK.toString());
        this.formatOptions.put("blue", ChatColor.BLUE.toString());
        this.formatOptions.put("bold", ChatColor.BOLD.toString());
        this.formatOptions.put("darkAqua", ChatColor.DARK_AQUA.toString());
        this.formatOptions.put("darkBlue", ChatColor.DARK_BLUE.toString());
        this.formatOptions.put("darkGray", ChatColor.DARK_GRAY.toString());
        this.formatOptions.put("darkGreen", ChatColor.DARK_GREEN.toString());
        this.formatOptions.put("darkPurple", ChatColor.DARK_PURPLE.toString());
        this.formatOptions.put("darkRed", ChatColor.DARK_RED.toString());
        this.formatOptions.put("gold", ChatColor.GOLD.toString());
        this.formatOptions.put("gray", ChatColor.GRAY.toString());
        this.formatOptions.put("green", ChatColor.GREEN.toString());
        this.formatOptions.put("italic", ChatColor.ITALIC.toString());
        this.formatOptions.put("lightPurple", ChatColor.LIGHT_PURPLE.toString());
        this.formatOptions.put("magic", ChatColor.MAGIC.toString());
        this.formatOptions.put("red", ChatColor.RED.toString());
        this.formatOptions.put("reset", ChatColor.RESET.toString());
        this.formatOptions.put("strikethrough", ChatColor.STRIKETHROUGH.toString());
        this.formatOptions.put("underline", ChatColor.UNDERLINE.toString());
        this.formatOptions.put("white", ChatColor.WHITE.toString());
        this.formatOptions.put("yellow", ChatColor.YELLOW.toString());
        this.formatOptions.put("heart", new FormatOption("\u2764", "", ""));
        this.formatOptions.put("heartSmall", new FormatOption("\u2665", "", ""));
        this.formatOptions.put("heartWhite", new FormatOption("\u2661", "", ""));
        this.formatOptions.put("heartRotated", new FormatOption("\u2765", "", ""));
        this.formatOptions.put("heartExclamation", new FormatOption("\u2763", "", ""));
        this.formatOptions.put("br", new FormatOption("\n", System.lineSeparator(), System.lineSeparator()));
        this.formatOptions.put("...", new FormatOption("\u2026", "...", "..."));
        this.formatOptions.put(" ", new FormatOption("  "));
        this.defaultConfiguration = ConfigurationFile.defaultConfiguration();
    }
    
    public abstract String getLanguage();
    
    public void loadDefaults() {
        PluginConfigurationFile.loadConfiguration(this.defaultConfiguration, this.getResource(), this.getCharset());
    }
    
    public FileConfiguration getDefaultConfig() {
        return this.defaultConfiguration;
    }
    
    public void loadValues(final FileConfiguration config) {
        this.loadValues(config, this.defaultConfiguration);
    }
    
    public abstract void loadValues(final FileConfiguration p0, final FileConfiguration p1);
    
    protected ChatMessage load(String path, final String defaultValue) {
        path = this.getLanguage() + "." + path;
        String value = this.getConfig().getString(path);
        if (value == null) {
            value = this.getDefaultConfig().getString(path, defaultValue);
        }
        return new FormattedChatMessage(value, defaultValue, this.formatOptions);
    }
    
    @Deprecated
    protected ChatMessage load(String path) {
        path = this.getLanguage() + "." + path;
        String value = this.getConfig().getString(path);
        if (value == null) {
            value = this.getDefaultConfig().getString(path);
        }
        return new FormattedChatMessage(value, value, this.formatOptions);
    }
    
    protected void addFormatOption(final String name, final CharSequence option) {
        this.formatOptions.put(name, option);
    }
}
