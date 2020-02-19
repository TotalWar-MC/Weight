package com.steffbeard.totalwar.core.mechanics.armor;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;

public class ArmorWeightCommands implements CommandExecutor
{
    private final ArmorWeightPlugin plugin;
    
    public ArmorWeightCommands(final ArmorWeightPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("weight").setExecutor((CommandExecutor)this);
        plugin.getCommand("weight").setTabCompleter((TabCompleter)new ArmorWeightTabCompleter());
        plugin.getCommand("armorweight").setExecutor((CommandExecutor)this);
        plugin.getCommand("armorweight").setTabCompleter((TabCompleter)new ArmorWeightTabCompleter());
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final ArmorWeightLanguage lang = this.plugin.getLanguage();
        final String lowerCase = command.getName().toLowerCase();
        switch (lowerCase) {
            case "weight": {
                if (!sender.hasPermission("armorweight.command.weight.getown")) {
                    lang.commandErrorNoPermission.send(sender, new Object[0]);
                    return true;
                }
                if (args.length == 0) {
                    if (!(sender instanceof Player)) {
                        lang.commandErrorMissingArgument.send(sender, "argType", lang.commandArgumentPlayer);
                        lang.commandErrorSyntax.send(sender, "syntax", "weight <" + lang.commandArgumentPlayer + ">");
                        return true;
                    }
                    final Player p = (Player)sender;
                    final WeightManager wm = this.plugin.getWeightManager();
                    if (!(p.getVehicle() instanceof Horse)) {
                        lang.weightGetSelf.send(sender, "player", p.getName(), "weight", wm.formatWeight(wm.getWeight(p)), "playerWeight", wm.formatWeight(wm.getPlayerWeight(p)), "armorWeight", wm.formatWeight(wm.getArmorWeight(p)));
                    }
                    else {
                        final Horse h = (Horse)p.getVehicle();
                        lang.weightGetSelfHorse.send(sender, "player", p.getName(), "weight", wm.formatWeight(wm.getWeight(h)), "horseWeight", wm.formatWeight(wm.getHorseWeight(h)), "passengerWeight", wm.formatWeight(wm.isHorsePassengerWeightEnabled() ? wm.getWeight(p) : 0.0), "armorWeight", wm.formatWeight(wm.getArmorWeight(h)));
                    }
                    return true;
                }
                else {
                    if (args[0].equalsIgnoreCase("help")) {
                        lang.weightHelpGeneral.send(sender, new Object[0]);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("update")) {
                        if (!sender.hasPermission("armorweight.command.plugin.debug")) {
                            lang.commandErrorNoPermission.send(sender, new Object[0]);
                            return true;
                        }
                        final WeightManager wm2 = this.plugin.getWeightManager();
                        if (!(sender instanceof Player)) {
                            lang.commandErrorNoPermission.send(sender, new Object[0]);
                            return true;
                        }
                        final Player p2 = (Player)sender;
                        p2.sendMessage("Previous weight: " + wm2.getWeight(p2));
                        p2.sendMessage("Updating " + (wm2.updateWeight(p2) ? (ChatColor.GREEN + "changed") : (ChatColor.RED + "did not change")) + " weight");
                        p2.sendMessage("Weight after update: " + wm2.getWeight(p2));
                        wm2.updateEffects(p2);
                        p2.sendMessage("Updated effects.");
                        return true;
                    }
                    else {
                        if (!sender.hasPermission("armorweight.command.weight.getothers")) {
                            lang.commandErrorNoPermission.send(sender, new Object[0]);
                            return true;
                        }
                        final Player p = Bukkit.getPlayer(args[0]);
                        if (p == null) {
                            lang.commandErrorNotPlayer.send(sender, "player", args[0]);
                            if (sender instanceof Player) {
                                lang.commandErrorSyntax.send(sender, "syntax", "weight [" + lang.commandArgumentPlayer + "]");
                            }
                            else {
                                lang.commandErrorSyntax.send(sender, "syntax", "weight <" + lang.commandArgumentPlayer + ">");
                            }
                            return true;
                        }
                        final WeightManager wm = this.plugin.getWeightManager();
                        if (!(p.getVehicle() instanceof Horse)) {
                            lang.weightGetOther.send(sender, "player", p.getName(), "weight", wm.formatWeight(wm.getWeight(p)), "playerWeight", wm.formatWeight(wm.getPlayerWeight(p)), "armorWeight", wm.formatWeight(wm.getArmorWeight(p)));
                        }
                        else {
                            final Horse h = (Horse)p.getVehicle();
                            lang.weightGetOtherHorse.send(sender, "player", p.getName(), "weight", wm.formatWeight(wm.getWeight(h)), "horseWeight", wm.formatWeight(wm.getHorseWeight(h)), "passengerWeight", wm.formatWeight(wm.isHorsePassengerWeightEnabled() ? wm.getWeight(p) : 0.0), "armorWeight", wm.formatWeight(wm.getArmorWeight(h)));
                        }
                        return true;
                    }
                }
            }
            case "armorweight": {
                if (!sender.hasPermission("armorweight.command.plugin.info")) {
                    lang.commandErrorNoPermission.send(sender, new Object[0]);
                    return true;
                }
                if (args.length == 0) {
                    lang.pluginInfo.send(sender, "version", this.plugin.getVersion(), "creator", "Zettelkasten");
                    lang.pluginWebsite.send(sender, "website", this.plugin.getWebsite());
                    return true;
                }
                final String lowerCase2 = args[0].toLowerCase();
                switch (lowerCase2) {
                    case "help": {
                        if (sender.hasPermission("armorweight.command.weight.getown")) {
                            if (sender instanceof Player) {
                                lang.commandHelpCommand.send(sender, "syntax", "weight [" + lang.commandArgumentPlayer + "]", "description", lang.weightHelpGet);
                            }
                            else {
                                lang.commandHelpCommand.send(sender, "syntax", "weight <" + lang.commandArgumentPlayer + ">", "description", lang.weightHelpGet);
                            }
                        }
                        if (sender.hasPermission("armorweight.command.plugin.debug") && sender instanceof Player) {
                            lang.commandHelpCommand.send(sender, "syntax", "weight update", "description", lang.pluginHelpDebugUpdate);
                        }
                        if (sender.hasPermission("armorweight.command.plugin.reload")) {
                            lang.commandHelpCommand.send(sender, "syntax", "armorweight reload", "description", lang.pluginHelpReload);
                        }
                        if (sender.hasPermission("armorweight.command.plugin.debug")) {
                            lang.commandHelpCommand.send(sender, "syntax", "armorweight debug", "description", lang.pluginHelpDebug);
                        }
                        return true;
                    }
                    case "reload":
                    case "rl": {
                        if (!sender.hasPermission("armorweight.command.plugin.reload")) {
                            lang.commandErrorNoPermission.send(sender, new Object[0]);
                            return true;
                        }
                        this.plugin.reload();
                        lang.pluginReload.send(sender, new Object[0]);
                        return true;
                    }
                    case "debug": {
                        if (!sender.hasPermission("armorweight.command.plugin.debug")) {
                            lang.commandErrorNoPermission.send(sender, new Object[0]);
                            return true;
                        }
                        final Server server = this.plugin.getServer();
                        final WeightManager wm = this.plugin.getWeightManager();
                        sender.sendMessage("Running " + this.plugin.getName() + " " + this.plugin.getVersion() + " on " + server.getName() + " " + server.getVersion());
                        sender.sendMessage("Loaded players: " + wm.getLoadedPlayers().size() + " (of " + server.getOnlinePlayers().size() + " total)");
                        sender.sendMessage("Loaded horses: " + wm.getLoadedHorses().size());
                        sender.sendMessage("Using PortableHorses: " + wm.isPortableHorsesEnabled());
                        return true;
                    }
                    default: {
                        lang.commandErrorInvalidArgument.send(sender, "arg", args[0]);
                        lang.commandErrorSyntax.send(sender, "syntax", "armorweight help");
                        return true;
                    }
                }
            }
            default: {
                return true;
            }
        }
    }
    
    public class ArmorWeightTabCompleter implements TabCompleter
    {
        public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
            final int length = (args.length == 0) ? 1 : args.length;
            final String lowerCase = command.getName().toLowerCase();
            switch (lowerCase) {
                case "weight": {
                    if (!sender.hasPermission("armorweight.command.weight.getown") || !sender.hasPermission("armorweight.command.weight.getothers")) {
                        return null;
                    }
                    final List<String> options = new ArrayList<String>(Bukkit.getOnlinePlayers().size());
                    for (final Player player : Bukkit.getOnlinePlayers()) {
                        options.add(player.getName());
                    }
                    if (sender.hasPermission("armorweight.command.plugin.debug")) {
                        options.add("update");
                    }
                    return options;
                }
                case "armorweight": {
                    switch (length) {
                        case 1: {
                            final List<String> options = new ArrayList<String>(2);
                            options.add("help");
                            if (sender.hasPermission("armorweight.command.plugin.reload")) {
                                options.add("reload");
                            }
                            if (sender.hasPermission("armorweight.command.plugin.debug")) {
                                options.add("debug");
                            }
                            return options;
                        }
                        default: {
                            return null;
                        }
                    }
                }
                default: {
                    return null;
                }
            }
        }
    }
}
