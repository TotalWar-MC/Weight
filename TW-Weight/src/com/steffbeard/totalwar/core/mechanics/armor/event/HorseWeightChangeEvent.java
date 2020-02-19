package com.steffbeard.totalwar.core.mechanics.armor.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class HorseWeightChangeEvent extends EntityEvent
{
    private static final HandlerList handlers;
    private double oldWeight;
    private double newWeight;
    
    public HorseWeightChangeEvent(final Horse horse, final double oldWeight, final double newWeight) {
        super((Entity)horse);
        this.oldWeight = oldWeight;
        this.newWeight = newWeight;
    }
    
    public Horse getEntity() {
        return (Horse)super.getEntity();
    }
    
    public double getOldWeight() {
        return this.oldWeight;
    }
    
    public double getNewWeight() {
        return this.newWeight;
    }
    
    public HandlerList getHandlers() {
        return HorseWeightChangeEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return HorseWeightChangeEvent.handlers;
    }
    
    public String toString() {
        return "HorseWeightChangeEvent [horse=" + this.getEntity() + ", oldWeight=" + this.oldWeight + ", newWeight=" + this.newWeight + "]";
    }
    
    static {
        handlers = new HandlerList();
    }
}
