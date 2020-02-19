package com.steffbeard.totalwar.core.mechanics.armor;

import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.entity.Horse;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.ess3.api.IEssentials;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

public class WeightListener implements Listener
{
    private WeightManager manager;
    
    public WeightListener(final WeightManager manager) {
        this.manager = manager;
    }
    
    public WeightManager getManager() {
        return this.manager;
    }
    
    public void setManager(final WeightManager manager) {
        this.manager = manager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (this.manager.isWorldEnabled(player.getWorld())) {
            this.manager.loadPlayer(player);
            final Plugin plugin = this.manager.getPlugin();
            final Server server = plugin.getServer();
            if (server.getPluginManager().isPluginEnabled("Essentials")) {
                final IEssentials ess = (IEssentials)server.getPluginManager().getPlugin("Essentials");
                if (!ess.getUser(player).isAuthorized("essentials.speed")) {
                    new BukkitRunnable() {
                        private int tick = 0;
                        
                        public void run() {
                            if (this.tick == 4) {
                                this.cancel();
                            }
                            WeightListener.this.manager.updateEffects(player);
                            ++this.tick;
                        }
                    }.runTaskTimer(plugin, 0L, 2L);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        this.manager.unloadPlayer(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        this.manager.updateWeight(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        if (this.manager.isWorldEnabled(player.getWorld())) {
            this.manager.loadPlayer(player);
            this.manager.updateEffects(player);
        }
        else {
            this.manager.unloadPlayer(player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        this.manager.updateWeightLater(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        this.onInventoryInteract((InventoryInteractEvent)event);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(final InventoryDragEvent event) {
        this.onInventoryInteract((InventoryInteractEvent)event);
    }
    
    public void onInventoryInteract(final InventoryInteractEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player) {
            this.manager.updateWeightLater((Player)holder);
        }
        if (holder instanceof Horse && this.manager.isHorseLoaded((Horse)holder)) {
            final Horse horse = (Horse)holder;
            this.manager.updateWeightLater(horse);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClickPortableHorsesFix(final InventoryClickEvent event) {
        if (!this.manager.isPortableHorsesEnabled()) {
            return;
        }
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player) {
            this.manager.updateEffects((Player)holder);
        }
        final Player player = (Player)event.getWhoClicked();
        if (!(holder instanceof Horse) || !this.manager.isHorseLoaded((Horse)holder)) {
            return;
        }
        final Horse horse = (Horse)holder;
        try {
            if (this.manager.isKickedOffHorse(player, horse, event.getCurrentItem())) {
                this.manager.unloadHorse(horse, null);
            }
        }
        catch (Exception e) {
            this.manager.getPlugin().getLogger().warning("Failed to perform PortableHorses fix:");
            e.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryDragPortableHorsesFix(final InventoryDragEvent event) {
        if (!this.manager.isPortableHorsesEnabled()) {
            return;
        }
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player) {
            this.manager.updateEffects((Player)holder);
        }
        final Player player = (Player)event.getWhoClicked();
        if (!(holder instanceof Horse) || !this.manager.isHorseLoaded((Horse)holder)) {
            return;
        }
        final Horse horse = (Horse)holder;
        try {
            for (final ItemStack item : event.getNewItems().values()) {
                if (this.manager.isKickedOffHorse(player, horse, item)) {
                    this.manager.unloadHorse(horse, null);
                }
            }
        }
        catch (Exception e) {
            this.manager.getPlugin().getLogger().warning("Failed to perform PortableHorses fix:");
            e.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        if (event.getVehicle() instanceof Horse) {
            this.manager.loadHorse((Horse)event.getVehicle(), event.getEntered());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event) {
        if (event.getVehicle() instanceof Horse) {
            this.manager.unloadHorse((Horse)event.getVehicle(), null);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
        final Player player = event.getPlayer();
        this.manager.updateWeightLater(player, true);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerVelocity(final PlayerVelocityEvent event) {
        this.manager.updateKnockbackEffect(event.getPlayer(), event.getVelocity());
    }
}
