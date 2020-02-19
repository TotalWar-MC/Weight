package com.steffbeard.totalwar.core.mechanics.armor;

import org.apache.commons.lang.Validate;

public enum ArmorPart
{
    BOOTS(0), 
    LEGGINGS(1), 
    CHESTPLATE(2), 
    HELMET(3);
    
    private final int id;
    private double weightShare;
    
    private ArmorPart(final int id) {
        this.id = id;
        this.weightShare = 0.25;
    }
    
    public int getId() {
        return this.id;
    }
    
    public static ArmorPart valueOf(final int id) {
        switch (id) {
            case 0: {
                return ArmorPart.BOOTS;
            }
            case 1: {
                return ArmorPart.LEGGINGS;
            }
            case 2: {
                return ArmorPart.CHESTPLATE;
            }
            case 3: {
                return ArmorPart.HELMET;
            }
            default: {
                return null;
            }
        }
    }
    
    public double getWeightShare() {
        return this.weightShare;
    }
    
    public void setWeightShare(final double weightShare) {
        this.weightShare = weightShare;
    }
    
    public static ArmorPart matchPart(final String name) {
        Validate.notNull((Object)name, "Name cannot be null");
        return valueOf(name.toUpperCase().replaceAll("\\s+", "_").replaceAll("\\W", ""));
    }
}
