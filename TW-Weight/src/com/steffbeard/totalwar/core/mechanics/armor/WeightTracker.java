package com.steffbeard.totalwar.core.mechanics.armor;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.steffbeard.totalwar.core.mechanics.armor.event.PlayerWeightChangeEvent;

import java.util.HashMap;
import org.bukkit.entity.Player;
import java.util.Map;
import org.bukkit.event.Listener;

public class WeightTracker implements Listener
{
    private final ArmorWeightPlugin plugin;
    private final WeightManager manager;
    private final ArmorWeightLanguage lang;
    private final ArmorWeightConfiguration config;
    private final Map<Player, Long> cooldownMap;
    private long cooldown;
    
    public WeightTracker(final ArmorWeightPlugin plugin, final WeightManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.lang = plugin.getLanguage();
        this.config = plugin.getConfiguration();
        this.cooldownMap = new HashMap<Player, Long>();
        this.cooldown = this.config.weightWarningCooldown();
    }
    
    public void register() {
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.cooldownMap.remove(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerWeightChange(final PlayerWeightChangeEvent event) {
        if (event.getNewWeight() <= event.getOldWeight()) {
            return;
        }
        if (!this.config.weightWarningEnabled()) {
            return;
        }
        final Player player = event.getPlayer();
        final long time = System.currentTimeMillis();
        final boolean canSendMessage = !this.cooldownMap.containsKey(player) || time - this.cooldownMap.get(player) >= this.cooldown;
        if (canSendMessage) {
            this.lang.weightWarning.send((CommandSender)player, "weight", this.manager.formatWeight(event.getNewWeight()), "oldWeight", this.manager.formatWeight(event.getOldWeight()));
            this.cooldownMap.put(player, time);
        }
    }
}
