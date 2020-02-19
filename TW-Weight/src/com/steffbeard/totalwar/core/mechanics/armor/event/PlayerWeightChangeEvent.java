package com.steffbeard.totalwar.core.mechanics.armor.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerWeightChangeEvent extends PlayerEvent
{
    private static final HandlerList handlers;
    private double oldWeight;
    private double newWeight;
    
    public PlayerWeightChangeEvent(final Player player, final double oldWeight, final double newWeight) {
        super(player);
        this.oldWeight = oldWeight;
        this.newWeight = newWeight;
    }
    
    public double getOldWeight() {
        return this.oldWeight;
    }
    
    public double getNewWeight() {
        return this.newWeight;
    }
    
    public HandlerList getHandlers() {
        return PlayerWeightChangeEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlayerWeightChangeEvent.handlers;
    }
    
    public String toString() {
        return "PlayerWeightChangeEvent [player=" + this.getPlayer() + ", oldWeight=" + this.oldWeight + ", newWeight=" + this.newWeight + "]";
    }
    
    static {
        handlers = new HandlerList();
    }
}
