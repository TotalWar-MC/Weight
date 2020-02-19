package com.steffbeard.totalwar.core.mechanics.armor;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.steffbeard.totalwar.core.mechanics.armor.chat.ChatMessage;
import com.steffbeard.totalwar.core.mechanics.armor.configuration.LanguageConfigurationFile;

public class ArmorWeightLanguage extends LanguageConfigurationFile
{
    private final ArmorWeightPlugin plugin;
    public ChatMessage commandErrorSyntax;
    public ChatMessage commandErrorInvalidArgument;
    public ChatMessage commandErrorInvalidArgumentType;
    public ChatMessage commandErrorMissingArgument;
    public ChatMessage commandErrorNotPlayer;
    public ChatMessage commandErrorNoPermission;
    public ChatMessage commandErrorDisabled;
    public ChatMessage commandArgumentAmount;
    public ChatMessage commandArgumentPlayer;
    public ChatMessage commandHelpCommand;
    public ChatMessage weightGetSelf;
    public ChatMessage weightGetSelfHorse;
    public ChatMessage weightGetOther;
    public ChatMessage weightGetOtherHorse;
    public ChatMessage weightHelpGeneral;
    public ChatMessage weightHelpGet;
    public ChatMessage weightWarning;
    public ChatMessage pluginPrefix;
    public ChatMessage pluginInfo;
    public ChatMessage pluginWebsite;
    public ChatMessage pluginReload;
    public ChatMessage pluginUpdateAvailable;
    public ChatMessage pluginHelpReload;
    public ChatMessage pluginHelpDebug;
    public ChatMessage pluginHelpDebugUpdate;
    
    public ArmorWeightLanguage(final ArmorWeightPlugin plugin, final String file, final String resource) {
        super((Plugin)plugin, file, resource);
        this.plugin = plugin;
    }
    
    @Override
    public String getLanguage() {
        return this.plugin.getConfiguration().chatLanguage();
    }
    
    @Override
    public void loadValues(final FileConfiguration config, final FileConfiguration defaults) {
        if (config.getBoolean("config.autoUpdate", true)) {
            this.update(config);
        }
        this.pluginPrefix = this.load("plugin.prefix", "&(darkGray)[&(gray)ArmorWeight&(gray)] ");
        this.addFormatOption("prefix", this.pluginPrefix.toFormatOption(new Object[0]));
        this.commandErrorSyntax = this.load("command.error.syntax", "&(red)Syntax: &(gray)/%(syntax)");
        this.commandErrorInvalidArgument = this.load("command.error.invalidArgument", "&(red)Argument \"%(arg)\" invalid");
        this.commandErrorInvalidArgumentType = this.load("command.error.invalidArgumentType", "&(red)Argument %(argType) \"%(arg)\" invalid");
        this.commandErrorMissingArgument = this.load("command.error.missingArgument", "&(red)Missing argument %(argType)");
        this.commandErrorNotPlayer = this.load("command.error.notPlayer", "&(red)The player %(player) is currently not online");
        this.commandErrorNoPermission = this.load("command.error.noPermission", "&(red)The player %(player) is currently not online");
        this.commandErrorDisabled = this.load("command.error.disabled", "&(red)You don't have permission to do this");
        this.commandArgumentAmount = this.load("command.argument.amount", "amount");
        this.commandArgumentPlayer = this.load("command.argument.player", "player");
        this.commandHelpCommand = this.load("command.help.command", "&(white)/%(syntax)&(white) - &(gray)%(description)");
        this.weightGetSelf = this.load("weight.get.self", "&(prefix)&(gray)You weigh &(gold)%(weight) &(yellow)(%(playerWeight) + %(armorWeight))&(gray)!");
        this.weightGetSelfHorse = this.load("weight.get.selfHorse", "&(prefix)&(gray)Your &(darkGray)horse &(gray)weighs &(gold)%(weight) &(yellow)(%(horseWeight) + %(armorWeight) + %(passengerWeight))&(gray)!");
        this.weightGetOther = this.load("weight.get.other", "&(prefix)&(gray)%(player) weighs &(gold)%(weight) &(yellow)(%(playerWeight) + %(armorWeight))&(gray)!");
        this.weightGetOtherHorse = this.load("weight.get.otherHorse", "&(prefix)&(gray)%(player)'s &(darkGray)horse &(gray)weighs &(gold)%(weight) &(yellow)(%(horseWeight) + %(armorWeight) + %(passengerWeight))&(gray)!");
        this.weightHelpGeneral = this.load("weight.help.general", "&(prefix)&(gray)Your &(yellow)weight &(gray)effects how &(yellow)fast &(gray)you can move.&(br)The more &(yellow)armor &(gray)you and your horse are wearing, the more your weight goes up.");
        this.weightHelpGet = this.load("weight.help.get", "Gets the weight of a player");
        this.weightWarning = this.load("weight.warning", "&(prefix)&(gray)Wearing heavy armor weighs you down");
        this.pluginInfo = this.load("plugin.info", "&(prefix)&(gray)This server is running &(darkGray)ArmorWeight&(gray) v%(version) by %(creator)!");
        this.pluginWebsite = this.load("plugin.website", "&(darkGray)Website: &(gray)%(website)");
        this.pluginReload = this.load("plugin.reload", "&(prefix)&(white)Reloaded configurations &(...)");
        this.pluginUpdateAvailable = this.load("plugin.updateAvailable", "&(prefix)&(gray)An update is available &(white)(%(updateName) for %(updateGameVersion))&(gray)!&(br)&(white)Download it at &(gray)%(updateLink)&(white).");
        this.pluginHelpReload = this.load("plugin.help.reload", "Reloads the plugin configurations");
        this.pluginHelpDebug = this.load("plugin.help.debug", "Prints debug information");
        this.pluginHelpDebugUpdate = this.load("plugin.help.debugUpdate", "Force recalculation of your weight");
    }
    
    public void update(final FileConfiguration config) {
        final String version = this.getVersion();
        switch (version) {
            default: {
                this.plugin.getLogger().warning("Unknown version of configuration file \"lang.yml\". Will not update file");
                break;
            }
            case "unknown":
            case "0.1.0": {
                this.updateStart("0.1.0");
                this.setIfNotExists("enUS.weight.get.selfHorse", "&(prefix)&(gray)Your &(darkGray)horse &(gray)weighs &(gold)%(weight) &(yellow)(%(horseWeight) + %(armorWeight) + %(passengerWeight))&(gray)!");
                this.setIfNotExists("enUS.weight.get.otherHorse", "&(prefix)&(gray)%(player)'s &(darkGray)horse &(gray)weighs &(gold)%(weight) &(yellow)(%(horseWeight) + %(armorWeight) + %(passengerWeight))&(gray)!");
            }
            case "0.2.0":
            case "0.2.1":
            case "0.3.0":
            case "0.3.1":
            case "0.3.2": {
                this.updateStart("0.3.2");
                this.setIfNotExists("enUS.weight.warning", "&(prefix)&(gray)Wearing heavy armor will cause you to slow down!");
            }
            case "0.3.3":
            case "0.3.4":
            case "0.3.5": {
                this.updateStart("0.3.5");
                this.setIfNotExists("enUS.weight.help.help.general", "&(prefix)&(gray)Your &(yellow)weight &(gray)effects how &(yellow)fast &(gray)you can move.&(br)The more &(yellow)armor &(gray)you and your horse are wearing, the more your weight goes up.");
            }
            case "0.3.6": {
                this.updateStart("0.3.6");
                this.setIfNotExists("enUS.plugin.help.debug", "Prints debug information");
            }
            case "0.3.7":
            case "0.3.8":
            case "0.3.9": {
                this.updateStart("0.3.9");
                this.setIfNotExists("enUS.plugin.help.debugUpdate", "Force recalculation of your weight");
            }
            case "0.3.10":
            case "0.3.11":
            case "0.3.12":
            case "0.3.13":
            case "0.3.14":
            case "0.3.15": {
                this.updateDone("0.3.15");
                break;
            }
        }
    }
    
    @Deprecated
    public ChatMessage pluginPrefix() {
        return this.pluginPrefix;
    }
}
