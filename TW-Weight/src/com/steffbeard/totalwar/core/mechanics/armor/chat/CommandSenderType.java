package com.steffbeard.totalwar.core.mechanics.armor.chat;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public enum CommandSenderType
{
    Player, 
    Console, 
    Block;
    
    public static CommandSenderType valueOf(final CommandSender sender) {
        if (sender instanceof Player) {
            return CommandSenderType.Player;
        }
        if (sender instanceof ConsoleCommandSender) {
            return CommandSenderType.Console;
        }
        if (sender instanceof BlockCommandSender) {
            return CommandSenderType.Block;
        }
        return null;
    }
}
