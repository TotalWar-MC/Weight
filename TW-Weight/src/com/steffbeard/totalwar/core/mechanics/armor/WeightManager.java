package com.steffbeard.totalwar.core.mechanics.armor;

import java.util.Collection;
import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import java.util.Arrays;
import org.bukkit.inventory.HorseInventory;
import java.util.Iterator;
import org.bukkit.util.Vector;

import com.steffbeard.totalwar.core.mechanics.armor.event.HorseWeightChangeEvent;
import com.steffbeard.totalwar.core.mechanics.armor.event.PlayerWeightChangeEvent;
import com.steffbeard.totalwar.core.mechanics.armor.event.ReflectionHandler;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.lang.Validate;
import org.bukkit.metadata.LazyMetadataValue;
import java.util.concurrent.Callable;
import org.bukkit.metadata.MetadataValue;
import java.lang.reflect.Method;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.enchantments.Enchantment;
import java.util.Map;

public class WeightManager
{
    public static final String METADATA_KEY = "weight";
    public static final double DEFAULT_PLAYER_WEIGHT = 1.0;
    public static double PLAYER_WEIGHT;
    public static final double DEFAULT_HORSE_WEIGHT = 5.0;
    public static double HORSE_WEIGHT;
    public static final float DEFAULT_WALK_SPEED = 0.2f;
    public static final float DEFAULT_FLY_SPEED = 0.1f;
    public static double DEFAULT_ENCHANTMENT_WEIGHT;
    public static final Map<Enchantment, Double> ENCHANTMENT_WEIGHTS;
    private final Plugin plugin;
    private final Logger logger;
    private Set<String> enabledWorlds;
    private boolean allWorldsEnabled;
    private boolean playerArmorWeightEnabled;
    private boolean horseArmorWeightEnabled;
    private boolean horsePassengerWeightEnabled;
    private boolean enchantmentWeightEnabled;
    private boolean playerSpeedEffectEnabled;
    private boolean playerCreativeSpeedEffectEnabled;
    private boolean horseSpeedEffectEnabled;
    private double speedEffectAmplifier;
    private boolean playerKnockbackEffectEnabled;
    private double knockbackEffectAmplifier;
    private boolean portableHorsesEnabled;
    private final Map<Player, PlayerData> playerData;
    private final Set<Player> loadedPlayers;
    private final Map<Horse, HorseData> horseData;
    private final Set<Horse> loadedHorses;
    private Class<?> craftHorseClass;
    private Class<?> entityHorseClass;
    private Class<?> iAttributeClass;
    private Class<?> genericAttributesClass;
    private Class<?> attributeInstanceClass;
    private String movementSpeedFieldName;
    private String[] movementSpeedFieldNames;
    private Class<?> portableHorsesClass;
    private Method portableHorsesClassIsPortableHorseSaddleMethod;
    private Method portableHorsesClassCanUseHorseMethod;
    
    private final MetadataValue makeWeightMetadata(final WeightData<?> data) {
        return (MetadataValue)new LazyMetadataValue(this.getPlugin(), (Callable)new Callable<Object>() {
            @Override
            public Object call() {
                return data.getStoredWeight();
            }
        });
    }
    
    public WeightManager(final Plugin plugin, final Logger logger) {
        this.speedEffectAmplifier = 1.0;
        this.knockbackEffectAmplifier = 1.0;
        this.movementSpeedFieldName = null;
        this.movementSpeedFieldNames = new String[] { "MOVEMENT_SPEED", "d" };
        Validate.notNull((Object)logger, "Logger cannot be null");
        this.plugin = plugin;
        this.logger = logger;
        this.enabledWorlds = new HashSet<String>();
        this.allWorldsEnabled = false;
        this.playerArmorWeightEnabled = true;
        this.horseArmorWeightEnabled = true;
        this.horsePassengerWeightEnabled = true;
        this.enchantmentWeightEnabled = false;
        this.playerSpeedEffectEnabled = true;
        this.playerCreativeSpeedEffectEnabled = false;
        this.horseSpeedEffectEnabled = false;
        this.playerKnockbackEffectEnabled = true;
        this.portableHorsesEnabled = false;
        this.playerData = new HashMap<Player, PlayerData>();
        this.loadedPlayers = new LoadedPlayersSet();
        this.horseData = new HashMap<Horse, HorseData>();
        this.loadedHorses = new LoadedHorsesSet();
    }
    
    protected void initialize() {
        if (this.isHorseSpeedEffectEnabled()) {
            try {
                this.craftHorseClass = ReflectionHandler.getClass("CraftHorse", ReflectionHandler.SubPackageType.ENTITY);
                this.entityHorseClass = ReflectionHandler.getClass("EntityHorse", ReflectionHandler.PackageType.MINECRAFT_SERVER);
                this.iAttributeClass = ReflectionHandler.getClass("IAttribute", ReflectionHandler.PackageType.MINECRAFT_SERVER);
                this.genericAttributesClass = ReflectionHandler.getClass("GenericAttributes", ReflectionHandler.PackageType.MINECRAFT_SERVER);
                this.attributeInstanceClass = ReflectionHandler.getClass("AttributeInstance", ReflectionHandler.PackageType.MINECRAFT_SERVER);
            }
            catch (Exception e) {
                this.setHorseSpeedEffectEnabled(false);
                this.logger.warning("Failed to get NMS classes for modifing horse speeds. You are probably using the wrong version of the plugin. Install the latest version and reload the server.");
                this.logger.warning("The horse speed effect has been disabled.");
                this.logger.warning("Error log:");
                e.printStackTrace();
            }
        }
        if (this.isPortableHorsesEnabled()) {
            try {
                this.portableHorsesClass = Class.forName("com.norcode.bukkit.portablehorses.PortableHorses");
                this.portableHorsesClassIsPortableHorseSaddleMethod = this.portableHorsesClass.getMethod("isPortableHorseSaddle", ItemStack.class);
                this.portableHorsesClassCanUseHorseMethod = this.portableHorsesClass.getMethod("canUseHorse", Player.class, Horse.class);
            }
            catch (Exception e) {
                this.setPortableHorsesEnabled(false);
                this.logger.warning("Failed to get PortableHorses classes to create compatibility with PortableHorses. You are probably using the wrong version of the plugin. Install the latest version and reload the server.");
                this.logger.warning("It is recommended to not use PortableHorses or disable the horse speed effect. Otherwise, horse speeds may be messed up.");
                this.logger.warning("Error log:");
                e.printStackTrace();
            }
        }
    }
    
    public Plugin getPlugin() {
        return this.plugin;
    }
    
    public Logger getLogger() {
        return this.logger;
    }
    
    public Set<String> getEnabledWorlds() {
        return this.enabledWorlds;
    }
    
    public boolean isWorldEnabled(final World world) {
        return this.isAllWorldsEnabled() || (world != null && this.enabledWorlds.contains(world.getName()));
    }
    
    public boolean isWorldEnabled(final String worldName) {
        return this.isAllWorldsEnabled() || this.enabledWorlds.contains(worldName);
    }
    
    public void setEnabledWorlds(final Set<String> enabledWorldNames) {
        Validate.notNull((Object)enabledWorldNames, "Enabled worlds cannot be null");
        this.enabledWorlds = enabledWorldNames;
    }
    
    public boolean enableWorld(final World world) {
        Validate.notNull((Object)world, "World cannot be null");
        return this.enableWorld(world.getName());
    }
    
    public boolean enableWorld(final String worldName) {
        Validate.notNull((Object)worldName, "World name cannot be null");
        return this.enabledWorlds.add(worldName);
    }
    
    public boolean disableWorld(final World world) {
        Validate.notNull((Object)world, "World cannot be null");
        return this.disableWorld(world.getName());
    }
    
    public boolean disableWorld(final String worldName) {
        Validate.notNull((Object)worldName, "World name cannot be null");
        return this.enabledWorlds.remove(worldName);
    }
    
    public boolean isAllWorldsEnabled() {
        return this.allWorldsEnabled;
    }
    
    public void setAllWorldsEnabled(final boolean enabled) {
        this.allWorldsEnabled = enabled;
    }
    
    public boolean isPortableHorsesEnabled() {
        return this.portableHorsesEnabled;
    }
    
    public void setPortableHorsesEnabled(final boolean enabled) {
        this.portableHorsesEnabled = enabled;
    }
    
    public boolean isPlayerArmorWeightEnabled() {
        return this.playerArmorWeightEnabled;
    }
    
    public void setPlayerArmorWeightEnabled(final boolean enabled) {
        this.playerArmorWeightEnabled = enabled;
    }
    
    public boolean isHorseArmorWeightEnabled() {
        return this.horseArmorWeightEnabled;
    }
    
    public void setHorseArmorWeightEnabled(final boolean enabled) {
        this.horseArmorWeightEnabled = enabled;
    }
    
    public boolean isHorsePassengerWeightEnabled() {
        return this.horsePassengerWeightEnabled;
    }
    
    public void setHorsePassengerWeightEnabled(final boolean horsePassengerWeightEnabled) {
        this.horsePassengerWeightEnabled = horsePassengerWeightEnabled;
    }
    
    public boolean isEnchantmentWeightEnabled() {
        return this.enchantmentWeightEnabled;
    }
    
    public void setEnchantmentWeightEnabled(final boolean enabled) {
        this.enchantmentWeightEnabled = enabled;
    }
    
    public boolean isPlayerSpeedEffectEnabled() {
        return this.playerSpeedEffectEnabled;
    }
    
    public void setPlayerSpeedEffectEnabled(final boolean enabled) {
        this.playerSpeedEffectEnabled = enabled;
    }
    
    public boolean isPlayerCreativeSpeedEffectEnabled() {
        return this.playerCreativeSpeedEffectEnabled;
    }
    
    public void setPlayerCreativeSpeedEffectEnabled(final boolean enabled) {
        this.playerCreativeSpeedEffectEnabled = enabled;
    }
    
    public boolean isHorseSpeedEffectEnabled() {
        return this.horseSpeedEffectEnabled;
    }
    
    public void setHorseSpeedEffectEnabled(final boolean enabled) {
        this.horseSpeedEffectEnabled = enabled;
    }
    
    public double getSpeedEffectAmplifier() {
        return this.speedEffectAmplifier;
    }
    
    public void setSpeedEffectAmplifier(final double amplifier) {
        this.speedEffectAmplifier = amplifier;
    }
    
    public boolean isPlayerKnockbackEffectEnabled() {
        return this.playerKnockbackEffectEnabled;
    }
    
    public void setPlayerKnockbackEffectEnabled(final boolean enabled) {
        this.playerKnockbackEffectEnabled = enabled;
    }
    
    public double getKnockbackEffectAmplifier() {
        return this.knockbackEffectAmplifier;
    }
    
    public void setKnockbackEffectAmplifier(final double amplifier) {
        this.knockbackEffectAmplifier = amplifier;
    }
    
    public String formatWeight(final double weight) {
        return String.valueOf(Math.round(weight * 100.0));
    }
    
    public boolean loadPlayer(final Player player) {
        Validate.notNull((Object)player, "Player cannot be null");
        if (this.isPlayerLoaded(player)) {
            return false;
        }
        final double weight = this.calculateWeight(player);
        final PlayerData data = new PlayerData(player, weight, allWorldsEnabled);
        data.init();
        this.playerData.put(player, data);
        this.updateEffects(player, weight);
        if (player.getVehicle() instanceof Horse) {
            this.loadHorse((Horse)player.getVehicle(), (Entity)player);
        }
        return true;
    }
    
    public boolean unloadPlayer(final Player player) {
        if (!this.isPlayerLoaded(player)) {
            return false;
        }
        this.resetEffects(player);
        this.playerData.get(player).destroy();
        this.playerData.remove(player);
        if (player.getVehicle() instanceof Horse) {
            this.unloadHorse((Horse)player.getVehicle(), null);
        }
        return true;
    }
    
    public boolean isPlayerLoaded(final Player player) {
        return this.playerData.containsKey(player);
    }
    
    public Set<Player> getLoadedPlayers() {
        return this.loadedPlayers;
    }
    
    public double getWeight(final Player player) {
        if (!this.isPlayerLoaded(player)) {
            return this.calculateWeight(player);
        }
        return this.playerData.get(player).getStoredWeight();
    }
    
    public double calculateWeight(final Player player) {
        return this.getPlayerWeight(player) + this.getArmorWeight(player);
    }
    
    public double getPlayerWeight(final Player player) {
        return WeightManager.PLAYER_WEIGHT;
    }
    
    public double getArmorWeight(final Player player) {
        Validate.notNull((Object)player, "Player cannot be null");
        if (!player.hasPermission("armorweight.weight.armor")) {
            return 0.0;
        }
        return this.getArmorWeight(player.getInventory().getArmorContents());
    }
    
    public double getArmorWeight(final ItemStack[] armorContents) {
        Validate.notNull((Object)armorContents, "Armor contents cannot be null");
        if (!this.isPlayerArmorWeightEnabled()) {
            return 0.0;
        }
        double weight = 0.0;
        for (int i = 0; i < armorContents.length; ++i) {
            final ItemStack stack = armorContents[i];
            final ArmorPart piece = ArmorPart.valueOf(i);
            weight += (ArmorType.getItemWeight(stack) + this.getEnchantmentsWeight(stack)) * piece.getWeightShare();
        }
        return weight;
    }
    
    public boolean updateWeight(final Player player) {
        return this.updateWeight(player, this.calculateWeight(player));
    }
    
    public boolean updateWeight(final Player player, final double weight) {
        Validate.notNull((Object)player, "Player cannot be null");
        if (!this.isPlayerLoaded(player)) {
            return false;
        }
        if (this.playerData.get(player).updateWeight(weight)) {
            this.updateEffects(player, weight);
            return true;
        }
        return false;
    }
    
    public void updateWeightLater(final Player player) {
        this.updateWeightLater(player, false);
    }
    
    public void updateWeightLater(final Player player, final boolean forceUpdateEffects) {
        Validate.notNull((Object)player, "Player cannot be null");
        this.plugin.getServer().getScheduler().runTask(this.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                if (!WeightManager.this.updateWeight(player) && forceUpdateEffects) {
                    WeightManager.this.updateEffects(player);
                }
            }
        });
    }
    
    public boolean updateEffects(final Player player) {
        return this.updateEffects(player, this.getWeight(player));
    }
    
    public boolean updateEffects(final Player player, final double weight) {
        Validate.notNull((Object)player, "Player cannot be null");
        if (!this.isPlayerLoaded(player)) {
            return false;
        }
        boolean speedEffect = player.hasPermission("armorweight.effect.speed");
        speedEffect = (speedEffect && ((player.getGameMode() == GameMode.CREATIVE) ? this.isPlayerCreativeSpeedEffectEnabled() : this.isPlayerSpeedEffectEnabled()));
        if (this.playerData.get(player).hadSpeedEffect() != speedEffect) {
            if (!speedEffect) {
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
            }
            this.playerData.get(player).setHadSpeedEffect(speedEffect);
        }
        if (speedEffect) {
            player.setWalkSpeed(this.getPlayerSpeed(weight, 0.20000000298023224));
            player.setFlySpeed(this.getPlayerSpeed(weight, 0.10000000149011612));
        }
        this.playerData.get(player).setDirty(false);
        return true;
    }
    
    @Deprecated
    public void resetWeight(final Player player) {
        this.resetEffects(player);
    }
    
    @Deprecated
    public void resetEffects(final Player player) {
        this.updateEffects(player, 1.0);
    }
    
    public Vector updateKnockbackEffect(final Player player, final Vector knockbackVelocity) {
        return this.updateKnockbackEffect(player, knockbackVelocity, this.getWeight(player));
    }
    
    public Vector updateKnockbackEffect(final Player player, final Vector knockbackVelocity, final double weight) {
        Validate.notNull((Object)player, "Player cannot be null");
        Validate.notNull((Object)knockbackVelocity, "Knockback velocity cannot be null");
        if (!this.isPlayerLoaded(player)) {
            return knockbackVelocity;
        }
        boolean speedEffect = player.hasPermission("armorweight.effect.speed");
        speedEffect = (speedEffect && player.getGameMode() != GameMode.CREATIVE && this.isPlayerKnockbackEffectEnabled());
        if (speedEffect) {
            final float velocityFactor = this.getPlayerKnockback(this.getWeight(player));
            knockbackVelocity.multiply(velocityFactor);
        }
        return knockbackVelocity;
    }
    
    public float getPlayerSpeed(final double weight, final double defaultSpeed) {
        final double amplifier = this.getSpeedEffectAmplifier();
        float speed = (float)(1.0 / (amplifier * weight) * defaultSpeed);
        if (speed > 1.0f) {
            speed = 1.0f;
        }
        if (speed < -1.0f) {
            speed = -1.0f;
        }
        return speed;
    }
    
    public float getPlayerKnockback(final double weight) {
        final double amplifier = this.getKnockbackEffectAmplifier();
        float factor = (float)(1.0 / (amplifier * weight));
        if (factor < 0.0f) {
            factor = 0.0f;
        }
        return factor;
    }
    
    public double getWeight(final Enchantment enchantment) {
        final Double d = WeightManager.ENCHANTMENT_WEIGHTS.containsKey(enchantment) ? WeightManager.ENCHANTMENT_WEIGHTS.get(enchantment) : WeightManager.DEFAULT_ENCHANTMENT_WEIGHT;
        return (d == null) ? 0.0 : d;
    }
    
    public double getWeight(final Enchantment enchantment, final int level) {
        Validate.isTrue(level >= 0, "Enchantment level has to be greater or equal than 0 (" + level + " < 0)");
        return this.getWeight(enchantment) * level;
    }
    
    public double getEnchantmentsWeight(final ItemStack item) {
        if (!this.isEnchantmentWeightEnabled()) {
            return 0.0;
        }
        if (item == null) {
            return 0.0;
        }
        double weight = 0.0;
        for (final Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            weight += this.getWeight(entry.getKey(), entry.getValue());
        }
        return weight;
    }
    
    public boolean loadHorse(final Horse horse) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        return this.loadHorse(horse, horse.getPassenger());
    }
    
    public boolean loadHorse(final Horse horse, final Entity passenger) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        if (this.isHorseLoaded(horse)) {
            return false;
        }
        try {
            final double weight = this.calculateWeight(horse, passenger);
            final HorseData data = new HorseData(horse, weight, allWorldsEnabled);
            if (this.isHorseSpeedEffectEnabled()) {
                data.setDefaultSpeed(this.getHorseSpeed(horse));
            }
            this.horseData.put(horse, data);
            this.updateEffects(horse, weight);
            return true;
        }
        catch (Exception e) {
            this.logger.warning("Failed to load horse " + horse + " and get its speed (probaly an NMS issue):");
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean unloadHorse(final Horse horse) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        return this.unloadHorse(horse, horse.getPassenger());
    }
    
    public boolean unloadHorse(final Horse horse, final Entity passenger) {
        if (!this.isHorseLoaded(horse)) {
            return false;
        }
        if (!this.resetEffects(horse)) {
            return false;
        }
        this.horseData.remove(horse);
        return true;
    }
    
    public boolean isHorseLoaded(final Horse horse) {
        return this.horseData.containsKey(horse);
    }
    
    public Set<Horse> getLoadedHorses() {
        return this.loadedHorses;
    }
    
    public double getWeight(final Horse horse) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        return this.getWeight(horse, horse.getPassenger());
    }
    
    public double getWeight(final Horse horse, final Entity passenger) {
        if (!this.isHorseLoaded(horse)) {
            return this.calculateWeight(horse, passenger);
        }
        return this.horseData.get(horse).getStoredWeight();
    }
    
    public double calculateWeight(final Horse horse) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        return this.calculateWeight(horse, horse.getPassenger());
    }
    
    public double calculateWeight(final Horse horse, final Entity passenger) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        double weight = this.getHorseWeight(horse);
        if (this.isHorsePassengerWeightEnabled() && passenger instanceof Player) {
            weight += this.getWeight((Player)passenger);
        }
        weight += this.getArmorWeight(horse.getInventory());
        return weight;
    }
    
    public double getHorseWeight(final Horse horse) {
        return WeightManager.HORSE_WEIGHT;
    }
    
    public double getArmorWeight(final Horse horse) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        return this.getArmorWeight(horse.getInventory());
    }
    
    public double getArmorWeight(final HorseInventory inventory) {
        Validate.notNull((Object)inventory, "Horse inventory cannot be null");
        if (!this.isHorseArmorWeightEnabled()) {
            return 0.0;
        }
        final ItemStack armor = inventory.getArmor();
        if (armor == null) {
            return 0.0;
        }
        double weight = ArmorType.getItemWeight(armor);
        weight += this.getEnchantmentsWeight(armor);
        return weight;
    }
    
    public boolean updateWeight(final Horse horse) {
        return this.updateWeight(horse, this.calculateWeight(horse));
    }
    
    public boolean updateWeight(final Horse horse, final Entity passenger) {
        return this.updateWeight(horse, this.calculateWeight(horse, passenger));
    }
    
    public boolean updateWeight(final Horse horse, final double weight) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        if (!this.isHorseLoaded(horse)) {
            return false;
        }
        if (this.horseData.get(horse).updateWeight(weight)) {
            this.updateEffects(horse, weight);
            return true;
        }
        return false;
    }
    
    public void updateWeightLater(final Horse horse) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        this.plugin.getServer().getScheduler().runTask(this.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                WeightManager.this.updateWeight(horse);
            }
        });
    }
    
    public void updateWeightLater(final Horse horse, final Entity passenger) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        this.plugin.getServer().getScheduler().runTask(this.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                WeightManager.this.updateWeight(horse, passenger);
            }
        });
    }
    
    public boolean updateEffects(final Horse horse) {
        return this.updateEffects(horse, this.getWeight(horse));
    }
    
    public boolean updateEffects(final Horse horse, final Entity passenger) {
        return this.updateEffects(horse, this.getWeight(horse, passenger));
    }
    
    public boolean updateEffects(final Horse horse, final double weight) {
        Validate.notNull((Object)horse, "Horse cannot be null");
        if (!this.isHorseLoaded(horse)) {
            return false;
        }
        if (this.isHorseSpeedEffectEnabled()) {
            try {
                this.setHorseSpeed(horse, this.getHorseSpeed(weight, this.getDefaultHorseSpeed(horse)));
            }
            catch (Exception e) {
                this.logger.warning("Failed to update speed of horse " + horse + " (probaly an NMS issue):");
                e.printStackTrace();
                return false;
            }
        }
        this.horseData.get(horse).setDirty(false);
        return true;
    }
    
    @Deprecated
    public boolean resetWeight(final Horse horse) {
        return this.resetEffects(horse);
    }
    
    @Deprecated
    public boolean resetEffects(final Horse horse) {
        return this.updateEffects(horse, 5.0);
    }
    
    public double getHorseSpeed(final double weight, final double defaultSpeed) {
        final double amplifier = this.getSpeedEffectAmplifier();
        double speed = 5.0 / (amplifier * weight) * defaultSpeed;
        if (speed > 1.0) {
            speed = 1.0;
        }
        if (speed < -1.0) {
            speed = -1.0;
        }
        return speed;
    }
    
    public double getDefaultHorseSpeed(final Horse horse) throws IllegalStateException {
        Validate.notNull((Object)horse, "Horse cannot be null");
        if (!this.isHorseLoaded(horse)) {
            throw new IllegalStateException("Horse has to be loaded");
        }
        return this.horseData.get(horse).getDefaultSpeed();
    }
    
    protected synchronized double getHorseSpeed(final Horse horse) throws Exception {
        Validate.notNull((Object)horse, "Horse cannot be null");
        final Object craftHorse = this.craftHorseClass.cast(horse);
        final Object entityHorse = this.craftHorseClass.getMethod("getHandle", (Class<?>[])new Class[0]).invoke(craftHorse, new Object[0]);
        final Object genericAttribute = this.genericAttributesClass.getField(this.getMovementSpeedFieldName()).get(null);
        final Object attributeInstance = this.entityHorseClass.getMethod("getAttributeInstance", this.iAttributeClass).invoke(entityHorse, genericAttribute);
        return (double)this.attributeInstanceClass.getMethod("getValue", (Class<?>[])new Class[0]).invoke(attributeInstance, new Object[0]);
    }
    
    protected synchronized void setHorseSpeed(final Horse horse, final double speed) throws Exception {
        Validate.notNull((Object)horse, "Horse cannot be null");
        final Object craftHorse = this.craftHorseClass.cast(horse);
        final Object entityHorse = this.craftHorseClass.getMethod("getHandle", (Class<?>[])new Class[0]).invoke(craftHorse, new Object[0]);
        final Object genericAttribute = this.genericAttributesClass.getField(this.getMovementSpeedFieldName()).get(null);
        final Object attributeInstance = this.entityHorseClass.getMethod("getAttributeInstance", this.iAttributeClass).invoke(entityHorse, genericAttribute);
        this.attributeInstanceClass.getMethod("setValue", Double.TYPE).invoke(attributeInstance, speed);
    }
    
    private String getMovementSpeedFieldName() throws Exception {
        if (this.movementSpeedFieldName == null) {
            final String[] movementSpeedFieldNames = this.movementSpeedFieldNames;
            final int length = movementSpeedFieldNames.length;
            int i = 0;
            while (i < length) {
                final String fieldName = movementSpeedFieldNames[i];
                try {
                    this.genericAttributesClass.getField(fieldName);
                    return this.movementSpeedFieldName = fieldName;
                }
                catch (NoSuchFieldException ex) {
                    ++i;
                    continue;
                }
            }
            throw new Exception("Generic movement speed attribute could not be found. Tried " + Arrays.toString(this.movementSpeedFieldNames) + ", but none of them worked.");
        }
        return this.movementSpeedFieldName;
    }
    
    protected boolean isKickedOffHorse(final Player player, final Horse horse, final ItemStack saddle) throws Exception {
        final Plugin ph = this.plugin.getServer().getPluginManager().getPlugin("PortableHorses");
        return (boolean)this.portableHorsesClassIsPortableHorseSaddleMethod.invoke(ph, saddle) && (boolean)this.portableHorsesClassCanUseHorseMethod.invoke(ph, player, horse);
    }
    
    static {
        WeightManager.PLAYER_WEIGHT = 1.0;
        WeightManager.HORSE_WEIGHT = 5.0;
        WeightManager.DEFAULT_ENCHANTMENT_WEIGHT = 0.01;
        ENCHANTMENT_WEIGHTS = new HashMap<Enchantment, Double>();
    }
    
    protected abstract class WeightData<T extends Entity>
    {
        private final T entity;
        private double weight;
        private boolean dirty;
        private final MetadataValue metadata;
        
        public WeightData(final WeightManager this$0, final T entity, final double weight) {
            this(entity, weight, true);
        }
        
        public WeightData(final T entity, final double weight, final boolean dirty) {
            this.entity = entity;
            this.weight = weight;
            this.dirty = dirty;
            this.metadata = WeightManager.this.makeWeightMetadata(this);
        }
        
        public T getEntity() {
            return this.entity;
        }
        
        public double getStoredWeight() {
            return this.weight;
        }
        
        public boolean updateWeight(final double weight) {
            if (this.weight != weight) {
                Bukkit.getPluginManager().callEvent(this.makeEvent(this.weight, weight));
                this.weight = weight;
                this.setDirty(true);
            }
            final boolean dirty = this.isDirty();
            if (dirty) {
                this.metadata.invalidate();
            }
            return dirty;
        }
        
        protected abstract Event makeEvent(final double p0, final double p1);
        
        public boolean isDirty() {
            return this.dirty;
        }
        
        public void setDirty(final boolean dirty) {
            this.dirty = dirty;
        }
        
        protected void init() {
            this.entity.setMetadata("weight", this.metadata);
        }
        
        protected void destroy() {
            this.entity.removeMetadata("weight", this.metadata.getOwningPlugin());
        }
    }
    
    protected class PlayerData extends WeightData<Player>
    {
        private boolean hadSpeed;
        
        public PlayerData(final WeightManager this$0, final Player player, final double weight) {
            this(player, weight, true);
        }
        
        public PlayerData(final Player player, final double weight, final boolean dirty) {
            super(player, weight, dirty);
            this.hadSpeed = true;
        }
        
        @Override
        protected Event makeEvent(final double oldWeight, final double newWeight) {
            return (Event)new PlayerWeightChangeEvent(this.getEntity(), oldWeight, newWeight);
        }
        
        public boolean hadSpeedEffect() {
            return this.hadSpeed;
        }
        
        public void setHadSpeedEffect(final boolean hadSpeedEffect) {
            this.hadSpeed = hadSpeedEffect;
        }
    }
    
    protected class HorseData extends WeightData<Horse>
    {
        private double defaultSpeed;
        
        public HorseData(final WeightManager this$0, final Horse horse, final double weight) {
            this(horse, weight, true);
        }
        
        public HorseData(final Horse horse, final double weight, final boolean dirty) {
            super(horse, weight, dirty);
            this.defaultSpeed = 0.0;
        }
        
        public double getDefaultSpeed() {
            return this.defaultSpeed;
        }
        
        public void setDefaultSpeed(final double speed) {
            this.defaultSpeed = speed;
        }
        
        @Override
        protected Event makeEvent(final double oldWeight, final double newWeight) {
            return (Event)new HorseWeightChangeEvent(this.getEntity(), oldWeight, newWeight);
        }
    }
    
    private class LoadedPlayersSet implements Set<Player>
    {
        private final Set<Player> base;
        
        private LoadedPlayersSet() {
            this.base = WeightManager.this.playerData.keySet();
        }
        
        @Override
        public int size() {
            return this.base.size();
        }
        
        @Override
        public boolean isEmpty() {
            return this.base.isEmpty();
        }
        
        @Override
        public boolean contains(final Object o) {
            return this.base.contains(o);
        }
        
        @Override
        public Iterator<Player> iterator() {
            return new LoadedPlayersIterator();
        }
        
        @Override
        public Object[] toArray() {
            return this.base.toArray();
        }
        
        @Override
        public <T> T[] toArray(final T[] a) {
            return this.base.toArray(a);
        }
        
        @Override
        public boolean add(final Player player) {
            return WeightManager.this.loadPlayer(player) && this.base.add(player);
        }
        
        @Override
        public boolean remove(final Object o) {
            return o instanceof Player && WeightManager.this.unloadPlayer((Player)o);
        }
        
        @Override
        public boolean containsAll(final Collection<?> c) {
            return this.base.containsAll(c);
        }
        
        @Override
        public boolean addAll(final Collection<? extends Player> players) {
            boolean mod = false;
            for (final Player player : players) {
                mod |= this.add(player);
            }
            return mod;
        }
        
        @Override
        public boolean retainAll(final Collection<?> c) {
            return this.base.retainAll(c);
        }
        
        @Override
        public boolean removeAll(final Collection<?> objects) {
            boolean mod = false;
            for (final Object obj : objects) {
                mod |= this.remove(obj);
            }
            return mod;
        }
        
        @Override
        public void clear() {
            final Iterator<Player> i = this.iterator();
            while (i.hasNext()) {
                this.remove(i.next());
            }
        }
        
        private class LoadedPlayersIterator implements Iterator<Player>
        {
            private final Iterator<Player> i;
            private Player current;
            
            private LoadedPlayersIterator() {
                this.i = LoadedPlayersSet.this.base.iterator();
            }
            
            @Override
            public boolean hasNext() {
                return this.i.hasNext();
            }
            
            @Override
            public Player next() {
                return this.current = this.i.next();
            }
            
            @Override
            public void remove() {
                if (this.current == null) {
                    throw new IllegalStateException();
                }
                LoadedPlayersSet.this.remove(this.current);
            }
        }
    }
    
    private class LoadedHorsesSet implements Set<Horse>
    {
        private final Set<Horse> base;
        
        private LoadedHorsesSet() {
            this.base = WeightManager.this.horseData.keySet();
        }
        
        @Override
        public int size() {
            return this.base.size();
        }
        
        @Override
        public boolean isEmpty() {
            return this.base.isEmpty();
        }
        
        @Override
        public boolean contains(final Object o) {
            return this.base.contains(o);
        }
        
        @Override
        public Iterator<Horse> iterator() {
            return new LoadedHorsesIterator();
        }
        
        @Override
        public Object[] toArray() {
            return this.base.toArray();
        }
        
        @Override
        public <T> T[] toArray(final T[] a) {
            return this.base.toArray(a);
        }
        
        @Override
        public boolean add(final Horse horse) {
            return WeightManager.this.loadHorse(horse) && this.base.add(horse);
        }
        
        @Override
        public boolean remove(final Object o) {
            return o instanceof Horse && WeightManager.this.unloadHorse((Horse)o);
        }
        
        @Override
        public boolean containsAll(final Collection<?> c) {
            return this.base.containsAll(c);
        }
        
        @Override
        public boolean addAll(final Collection<? extends Horse> horses) {
            boolean mod = false;
            for (final Horse horse : horses) {
                mod |= this.add(horse);
            }
            return mod;
        }
        
        @Override
        public boolean retainAll(final Collection<?> c) {
            return this.base.retainAll(c);
        }
        
        @Override
        public boolean removeAll(final Collection<?> objects) {
            boolean mod = false;
            for (final Object obj : objects) {
                mod |= this.remove(obj);
            }
            return mod;
        }
        
        @Override
        public void clear() {
            final Iterator<Horse> i = this.iterator();
            while (i.hasNext()) {
                this.remove(i.next());
            }
        }
        
        private class LoadedHorsesIterator implements Iterator<Horse>
        {
            private final Iterator<Horse> i;
            private Horse current;
            
            private LoadedHorsesIterator() {
                this.i = LoadedHorsesSet.this.base.iterator();
            }
            
            @Override
            public boolean hasNext() {
                return this.i.hasNext();
            }
            
            @Override
            public Horse next() {
                return this.current = this.i.next();
            }
            
            @Override
            public void remove() {
                if (this.current == null) {
                    throw new IllegalStateException();
                }
                LoadedHorsesSet.this.remove(this.current);
            }
        }
    }
}
