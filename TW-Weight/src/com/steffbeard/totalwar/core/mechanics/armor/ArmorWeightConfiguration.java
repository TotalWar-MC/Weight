package com.steffbeard.totalwar.core.mechanics.armor;

import java.util.Arrays;
import com.google.common.base.CaseFormat;
import com.steffbeard.totalwar.core.mechanics.armor.configuration.PluginConfigurationFile;

import org.bukkit.Material;
import java.util.HashSet;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import org.bukkit.enchantments.Enchantment;
import java.util.Map;
import org.bukkit.plugin.Plugin;

public class ArmorWeightConfiguration extends PluginConfigurationFile
{
    private final Plugin plugin;
    private final WeightManager manager;
    private boolean metricsEnabled;
    private String chatLanguage;
    private boolean weightWarningEnabled;
    private long weightWarningCooldown;
    private final Map<String, Enchantment> enchantmentMappings;
    
    public ArmorWeightConfiguration(final Plugin plugin, final String file, final String resource, final WeightManager manager) {
        super(plugin, file, resource);
        this.plugin = plugin;
        this.manager = manager;
        this.enchantmentMappings = new HashMap<String, Enchantment>();
        try {
            this.enchantmentMappings.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
            this.enchantmentMappings.put("fireProtection", Enchantment.PROTECTION_FIRE);
            this.enchantmentMappings.put("featherFalling", Enchantment.PROTECTION_FALL);
            this.enchantmentMappings.put("blastProtection", Enchantment.PROTECTION_EXPLOSIONS);
            this.enchantmentMappings.put("projectileProtection", Enchantment.PROTECTION_PROJECTILE);
            this.enchantmentMappings.put("respiration", Enchantment.OXYGEN);
            this.enchantmentMappings.put("aquaAffinity", Enchantment.WATER_WORKER);
            this.enchantmentMappings.put("sharpness", Enchantment.DAMAGE_ALL);
            this.enchantmentMappings.put("smite", Enchantment.DAMAGE_UNDEAD);
            this.enchantmentMappings.put("baneOfArthropods", Enchantment.DAMAGE_ARTHROPODS);
            this.enchantmentMappings.put("knockback", Enchantment.KNOCKBACK);
            this.enchantmentMappings.put("fireAspect", Enchantment.FIRE_ASPECT);
            this.enchantmentMappings.put("looting", Enchantment.LOOT_BONUS_MOBS);
            this.enchantmentMappings.put("efficiency", Enchantment.DIG_SPEED);
            this.enchantmentMappings.put("silkTouch", Enchantment.SILK_TOUCH);
            this.enchantmentMappings.put("unbreaking", Enchantment.DURABILITY);
            this.enchantmentMappings.put("fortune", Enchantment.LOOT_BONUS_BLOCKS);
            this.enchantmentMappings.put("power", Enchantment.ARROW_DAMAGE);
            this.enchantmentMappings.put("punch", Enchantment.ARROW_KNOCKBACK);
            this.enchantmentMappings.put("flame", Enchantment.ARROW_FIRE);
            this.enchantmentMappings.put("infinity", Enchantment.ARROW_INFINITE);
            this.enchantmentMappings.put("thorns", Enchantment.THORNS);
            this.enchantmentMappings.put("luckOfTheSea", Enchantment.LUCK);
            this.enchantmentMappings.put("lure", Enchantment.LURE);
            this.enchantmentMappings.put("frostWalker", Enchantment.FROST_WALKER);
            this.enchantmentMappings.put("mending", Enchantment.MENDING);
            this.enchantmentMappings.put("curseOfBinding", Enchantment.BINDING_CURSE);
            this.enchantmentMappings.put("curseOfVanishing", Enchantment.VANISHING_CURSE);
            this.enchantmentMappings.put("sweepingEdge", Enchantment.SWEEPING_EDGE);
        }
        catch (NoSuchFieldError noSuchFieldError) {}
    }
    
    @Override
    protected synchronized void loadValues(final FileConfiguration config) {
        if (config.getBoolean("config.autoUpdate", true)) {
            this.update(config);
        }
        this.metricsEnabled = config.getBoolean("metricsEnabled", false);
        WeightManager.PLAYER_WEIGHT = config.getDouble("playerWeight", 90.0) / 100.0;
        WeightManager.HORSE_WEIGHT = config.getDouble("horseWeight", 400.0) / 100.0;
        this.manager.setPlayerArmorWeightEnabled(config.getBoolean("weightEnabled.armor.player", true));
        this.manager.setHorseArmorWeightEnabled(config.getBoolean("weightEnabled.armor.horse", true));
        this.manager.setHorsePassengerWeightEnabled(config.getBoolean("weightEnabled.horseRider", true));
        this.manager.setEnchantmentWeightEnabled(config.getBoolean("weightEnabled.enchantment", false));
        this.manager.setPlayerSpeedEffectEnabled(config.getBoolean("effectEnabled.speed.player", true));
        this.manager.setPlayerCreativeSpeedEffectEnabled(config.getBoolean("effectEnabled.speed.playerCreative", false));
        this.manager.setHorseSpeedEffectEnabled(config.getBoolean("effectEnabled.speed.horse", false));
        this.manager.setSpeedEffectAmplifier(config.getDouble("effectEnabled.speed.amplifier", 1.0));
        this.manager.setPlayerKnockbackEffectEnabled(config.getBoolean("effectEnabled.knockback.player", true));
        this.manager.setKnockbackEffectAmplifier(config.getDouble("effectEnabled.knockback.amplifier", 1.0));
        this.manager.setEnabledWorlds(new HashSet<String>());
        this.manager.setAllWorldsEnabled(false);
            this.manager.setAllWorldsEnabled(true);
        
        ArmorType.reset();
        if (config.isConfigurationSection("armor.weight")) {
            for (final Map.Entry<String, Object> entry : config.getConfigurationSection("armor.weight").getValues(false).entrySet()) {
                this.loadArmorTypeWeight(entry.getKey(), entry.getValue());
            }
        }
        else {
            this.plugin.getLogger().warning("Invalid configuration for \"armor.weight\" (should be a section); not loading armor weights");
        }
        final double helmetShare = config.getDouble("armor.share.helmet", 17.0);
        final double chestplateShare = config.getDouble("armor.share.chestplate", 45.0);
        final double leggingsShare = config.getDouble("armor.share.leggings", 25.0);
        final double bootsShare = config.getDouble("armor.share.boots", 13.0);
        final double shareSum = helmetShare + chestplateShare + leggingsShare + bootsShare;
        ArmorPart.CHESTPLATE.setWeightShare(chestplateShare / shareSum);
        ArmorPart.LEGGINGS.setWeightShare(leggingsShare / shareSum);
        ArmorPart.HELMET.setWeightShare(helmetShare / shareSum);
        ArmorPart.BOOTS.setWeightShare(bootsShare / shareSum);
        this.chatLanguage = config.getString("chat.language", "enUS");
        WeightManager.DEFAULT_ENCHANTMENT_WEIGHT = 0.0;
        WeightManager.ENCHANTMENT_WEIGHTS.clear();
        if (this.manager.isEnchantmentWeightEnabled()) {
            if (config.isConfigurationSection("enchantment.weight")) {
                for (final Map.Entry<String, Object> entry2 : config.getConfigurationSection("enchantment.weight").getValues(false).entrySet()) {
                    this.loadEnchantmentWeight(entry2.getKey(), entry2.getValue());
                }
            }
            else {
                this.manager.setEnchantmentWeightEnabled(false);
                this.plugin.getLogger().warning("Invalid configuration for \"enchantment.weight\" (should be a section); not using enchantments weights");
            }
        }
        this.weightWarningEnabled = config.getBoolean("weightWarning.enabled", true);
        this.weightWarningCooldown = (long)(config.getDouble("weightWarning.cooldown", 10.0) * 1000.0);
    }
    
    private boolean loadArmorTypeWeight(final String name, final Object value) {
        if (ArmorType.contains(name)) {
            try {
                ArmorType.valueOf(name).setWeight(Double.valueOf(value.toString()) / 100.0);
                return true;
            }
            catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Armor weight value for " + name + " is an invalid number; ignoring it");
                return false;
            }
        }
        final Material material = Material.matchMaterial(name);
        if (material == null) {
            this.plugin.getLogger().warning("Armor weight key for " + name + " is not a material or armor type; ignoring it");
            return false;
        }
        try {
            final ArmorType type = new ArmorType(material);
            type.setWeight(Double.valueOf(value.toString()) / 100.0);
            ArmorType.register(type);
            return true;
        }
        catch (NumberFormatException e2) {
            this.plugin.getLogger().warning("Armor weight value for " + name + " is an invalid number; ignoring it");
            return false;
        }
    }
    
    private boolean loadEnchantmentWeight(final String name, final Object value) {
        if (name.equalsIgnoreCase("general")) {
            try {
                WeightManager.DEFAULT_ENCHANTMENT_WEIGHT = Double.valueOf(value.toString()) / 100.0;
                return true;
            }
            catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Enchantment weight value for " + name + " is an invalid number; ignoring it");
                return false;
            }
        }
        Enchantment ench = Enchantment.getByName(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name));
        if (ench == null) {
            ench = this.enchantmentMappings.get(name);
            if (ench == null) {
                this.plugin.getLogger().warning("Unknown enchantment type \"" + name + "\"; ignoring it");
                return false;
            }
        }
        try {
            WeightManager.ENCHANTMENT_WEIGHTS.put(ench, Double.valueOf(value.toString()) / 100.0);
            return true;
        }
        catch (NumberFormatException e2) {
            this.plugin.getLogger().warning("Enchantment weight value for " + name + " is an invalid number; ignoring it");
            return false;
        }
    }
    
    @Override
    protected void update(final FileConfiguration config) {
        final String version = this.getVersion();
        switch (version) {
            default: {
                this.plugin.getLogger().warning("Unknown version of configuration file \"config.yml\". Will not update file");
                break;
            }
            case "unknown":
            case "0.1.0": {
                this.setIfNotExists("horseWeight", 400);
            }
            case "0.2.0": {
                this.updateStart("0.2.0");
                this.setIfNotExists("enchantment.enabled", false);
                this.setIfNotExists("enchantment.weight.general", 1);
                this.setIfNotExists("enchantment.weight.protection", 2);
                this.setIfNotExists("enchantment.weight.unbreaking", 2);
            }
            case "0.2.1": {
                this.updateStart("0.2.1");
                this.setIfNotExists("weightEnabled.armor.player", true);
                this.setIfNotExists("weightEnabled.armor.horse", true);
                this.setIfNotExists("weightEnabled.horseRider", true);
                final boolean enchantmentEnabled = config.getBoolean("enchantment.enabled", true);
                this.setIfNotExists("weightEnabled.enchantment", enchantmentEnabled);
                if (config.contains("enchantment.enabled")) {
                    config.set("enchantment.enabled", (Object)null);
                }
                this.setIfNotExists("effectEnabled.speed.player", true);
                this.setIfNotExists("effectEnabled.speed.horse", true);
                this.setIfNotExists("enabledWorlds", Arrays.asList("world", "world_nether", "world_the_end", "*"));
            }
            case "0.3.0":
            case "0.3.1": {
                this.updateStart("0.3.1");
                this.setIfNotExists("effectEnabled.speed.playerCreative", false);
            }
            case "0.3.2": {
                this.updateStart("0.3.2");
                this.setIfNotExists("weightWarning.enabled", true);
                this.setIfNotExists("weightWarning.cooldown", 10);
            }
            case "0.3.3":
            case "0.3.4":
            case "0.3.5":
            case "0.3.6":
            case "0.3.7":
            case "0.3.8":
            case "0.3.9":
            case "0.3.10":
            case "0.3.11":
            case "0.3.12":
            case "0.3.13":
            case "0.3.14": {
                this.updateStart("0.3.14");
                this.setIfNotExists("effectEnabled.speed.amplifier", 1.0);
                this.setIfNotExists("effectEnabled.knockback.amplifier", 1.0);
                this.setIfNotExists("effectEnabled.knockback.player", true);
            }
            case "0.3.15": {
                this.updateDone("0.3.15");
                break;
            }
        }
    }
    
    public String chatLanguage() {
        return this.chatLanguage;
    }
    
    public boolean metricsEnabled() {
        return this.metricsEnabled;
    }
    
    public boolean weightWarningEnabled() {
        return this.weightWarningEnabled;
    }
    
    public long weightWarningCooldown() {
        return this.weightWarningCooldown;
    }
}
