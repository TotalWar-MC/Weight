package com.steffbeard.totalwar.core.mechanics.armor.chat;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class FormatOption implements CharSequence
{
    private final Map<CommandSenderType, String> values;
    private final String defaultValue;
    
    public FormatOption(final String playerValue) {
        (this.values = new HashMap<CommandSenderType, String>()).put(CommandSenderType.Player, playerValue);
        this.defaultValue = playerValue;
    }
    
    public FormatOption(final String playerValue, final String consoleValue) {
        this(playerValue);
        this.values.put(CommandSenderType.Console, consoleValue);
    }
    
    public FormatOption(final String playerValue, final String consoleValue, final String blockValue) {
        this(playerValue);
        this.values.put(CommandSenderType.Console, consoleValue);
        this.values.put(CommandSenderType.Block, blockValue);
    }
    
    public FormatOption(final Map<CommandSenderType, String> values) {
        this.defaultValue = values.get(CommandSenderType.Player);
        this.values = values;
    }
    
    @Override
    public char charAt(final int index) {
        return this.defaultValue.charAt(index);
    }
    
    @Override
    public int length() {
        return this.defaultValue.length();
    }
    
    @Override
    public CharSequence subSequence(final int beginIndex, final int endIndex) {
        return this.defaultValue.subSequence(beginIndex, endIndex);
    }
    
    @Override
    public boolean equals(final Object other) {
        return other == this || (other != null && other.toString().equals(this.toString()));
    }
    
    @Override
    public String toString() {
        return this.defaultValue;
    }
    
    public String toString(final CommandSenderType senderType) {
        return this.values.containsKey(senderType) ? this.values.get(senderType) : this.defaultValue;
    }
    
    public String toString(final CommandSender sender) {
        return this.toString(CommandSenderType.valueOf(sender));
    }
}
