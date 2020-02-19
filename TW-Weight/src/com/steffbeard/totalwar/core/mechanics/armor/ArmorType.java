package com.steffbeard.totalwar.core.mechanics.armor;

import java.util.HashMap;
import java.util.Arrays;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.lang.Validate;
import java.util.Collections;
import java.util.Collection;
import org.bukkit.Material;
import java.util.Map;

public class ArmorType
{
    private static final Map<String, ArmorType> registeredNames;
    private static final Map<Material, ArmorType> registeredMaterials;
    public static final ArmorType AIR;
    public static final ArmorType LEATHER;
    public static final ArmorType GOLD;
    public static final ArmorType CHAINMAIL;
    public static final ArmorType IRON;
    public static final ArmorType DIAMOND;
    private final String name;
    private final Material[] materials;
    private double weight;
    
    public static Collection<ArmorType> values() {
        return Collections.unmodifiableCollection((Collection<? extends ArmorType>)ArmorType.registeredNames.values());
    }
    
    public static ArmorType valueOf(final String name) {
        return ArmorType.registeredNames.get(name.toLowerCase());
    }
    
    public static boolean contains(final String name) {
        return ArmorType.registeredNames.containsKey(name);
    }
    
    public static ArmorType valueOf(final Material material) {
        final ArmorType type = ArmorType.registeredMaterials.get(material);
        return (type == null) ? ArmorType.AIR : type;
    }
    
    public static boolean contains(final Material material) {
        return ArmorType.registeredMaterials.containsKey(material);
    }
    
    public static void register(final ArmorType type) {
        Validate.notNull((Object)type, "Type cannot be null");
        Validate.isTrue(!ArmorType.registeredNames.containsKey(type.getName().toLowerCase()), "Type name already registered");
        ArmorType.registeredNames.put(type.getName().toLowerCase(), type);
        for (final Material mat : type.getMaterial()) {
            if (ArmorType.registeredMaterials.containsKey(mat)) {}
            ArmorType.registeredMaterials.put(mat, type);
        }
    }
    
    public static boolean registerIfAvailable(final String name, final String... materialNames) {
        Validate.notNull((Object)name, "Type name cannot be null");
        Validate.notNull((Object)materialNames, "Material names cannot be null");
        final List<Material> materials = new ArrayList<Material>(materialNames.length);
        for (final String materialName : materialNames) {
            try {
                final Material material = Material.valueOf(materialName);
                materials.add(material);
            }
            catch (IllegalArgumentException ex) {}
        }
        register(new ArmorType(name, (Material[])materials.toArray(new Material[materials.size()])));
        return true;
    }
    
    public static void reset() {
        ArmorType.registeredNames.clear();
        ArmorType.registeredMaterials.clear();
        register(ArmorType.AIR);
        register(ArmorType.LEATHER);
        register(ArmorType.GOLD);
        register(ArmorType.CHAINMAIL);
        register(ArmorType.IRON);
        register(ArmorType.DIAMOND);
    }
    
    public ArmorType(final Material material) {
        this(material.toString().toLowerCase(), new Material[] { material });
    }
    
    public ArmorType(final String name, final Material... contentMaterials) {
        this.name = name;
        this.materials = contentMaterials;
        this.weight = 1.0;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Material[] getMaterial() {
        return this.materials;
    }
    
    @Deprecated
    public double getWeight() {
        return this.weight;
    }
    
    public void setWeight(final double weight) {
        this.weight = weight;
    }
    
    public double getWeight(final ItemStack item) {
        return this.getWeight();
    }
    
    @Override
    public String toString() {
        return "ArmorType [name=" + this.name + ",materials=" + Arrays.toString(this.materials) + "]";
    }
    
    public static double getItemWeight(final ItemStack item) {
        return valueOf((item == null) ? null : item.getType()).getWeight(item);
    }
    
    static {
        registeredNames = new HashMap<String, ArmorType>();
        registeredMaterials = new HashMap<Material, ArmorType>();
        (AIR = new ArmorType("none", new Material[] { Material.AIR })).setWeight(0.0);
        LEATHER = new ArmorType("leather", new Material[] { Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS });
        GOLD = new ArmorType("gold", new Material[] { Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.GOLD_BARDING });
        CHAINMAIL = new ArmorType("chainmail", new Material[] { Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS });
        IRON = new ArmorType("iron", new Material[] { Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_BARDING });
        DIAMOND = new ArmorType("diamond", new Material[] { Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_BARDING });
        reset();
    }
}
