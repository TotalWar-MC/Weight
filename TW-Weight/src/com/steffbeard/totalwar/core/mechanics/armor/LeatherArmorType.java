package com.steffbeard.totalwar.core.mechanics.armor;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class LeatherArmorType extends ArmorType
{
    public LeatherArmorType(final Material material) {
        super(material);
    }
    
    public LeatherArmorType(final String name, final Material... contentMaterials) {
        super(name, contentMaterials);
    }
    
    @Override
    public double getWeight(final ItemStack item) {
        return super.getWeight(item);
    }
}
