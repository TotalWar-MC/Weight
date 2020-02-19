package com.steffbeard.totalwar.core.mechanics.armor.chat;

import org.bukkit.command.CommandSender;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class MessageValueMap extends HashMap<String, CharSequence>
{
    private static final long serialVersionUID = 1L;
    private Map<CommandSenderType, Map<String, String>> messageValues;
    
    public MessageValueMap() {
        this.messageValues = new HashMap<CommandSenderType, Map<String, String>>();
        for (final CommandSenderType type : CommandSenderType.values()) {
            this.messageValues.put(type, new HashMap<String, String>());
        }
    }
    
    @Override
    public CharSequence put(final String key, final CharSequence value) {
        if (value instanceof FormatOption) {
            final FormatOption f = (FormatOption)value;
            for (final CommandSenderType type : CommandSenderType.values()) {
                this.messageValues.get(type).put(key, f.toString(type));
            }
        }
        else {
            final String str = value.toString();
            for (final CommandSenderType type : CommandSenderType.values()) {
                this.messageValues.get(type).put(key, str);
            }
        }
        return super.put(key, value);
    }
    
    @Override
    public CharSequence remove(final Object key) {
        this.messageValues.remove(key);
        return super.remove(key);
    }
    
    public void recalculate() {
        this.messageValues = new HashMap<CommandSenderType, Map<String, String>>();
        for (final CommandSenderType type : CommandSenderType.values()) {
            this.messageValues.put(type, new HashMap<String, String>());
        }
        for (final Map.Entry<String, CharSequence> entry : this.entrySet()) {
            final String key = entry.getKey();
            final CharSequence charSequence = entry.getValue();
            if (charSequence instanceof FormatOption) {
                final FormatOption f = (FormatOption)charSequence;
                for (final CommandSenderType type2 : CommandSenderType.values()) {
                    this.messageValues.get(type2).put(key, f.toString(type2));
                }
            }
            else {
                final String value = charSequence.toString();
                for (final CommandSenderType type2 : CommandSenderType.values()) {
                    this.messageValues.get(type2).put(key, value);
                }
            }
        }
    }
    
    public Map<CommandSenderType, Map<String, String>> getMessageValues() {
        return this.messageValues;
    }
    
    public Map<String, String> getMessageValues(final CommandSenderType senderType) {
        return this.messageValues.get(senderType);
    }
    
    public Map<String, String> getMessageValues(final CommandSender sender) {
        return this.getMessageValues(CommandSenderType.valueOf(sender));
    }
    
    public static MessageValueMap valueOf(final Map<String, String> formatOptions) {
        final MessageValueMap map = new MessageValueMap();
        for (final Map.Entry<String, String> entry : formatOptions.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
    
    public static MessageValueMap valueOf(final Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Value array has to have an even number of elements.");
        }
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i].toString(), values[i + 1].toString());
        }
        return valueOf(map);
    }
}
