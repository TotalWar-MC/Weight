package com.steffbeard.totalwar.core.mechanics.armor.chat;

import java.util.HashMap;
import org.bukkit.command.CommandSender;

import com.steffbeard.totalwar.core.mechanics.armor.chat.ChatMessageInstance;
import com.steffbeard.totalwar.core.mechanics.armor.chat.CommandSenderType;
import com.steffbeard.totalwar.core.mechanics.armor.chat.FormatOption;

import org.bukkit.ChatColor;
import java.util.Map;
import org.apache.commons.lang.text.StrSubstitutor;

public class ChatMessage
{
    public static final ChatMessage NONE;
    private final String rawMessage;
    private final boolean nullMessage;
    
    public ChatMessage(final String rawMessage) {
        this.rawMessage = this.escapeCharacters(rawMessage);
        this.nullMessage = this.checkNullMessage();
    }
    
    protected boolean checkNullMessage() {
        return this.rawMessage == null || this.rawMessage.equalsIgnoreCase("null") || this.rawMessage.equalsIgnoreCase("none");
    }
    
    public String format(final CommandSenderType senderType, final MessageValueMap values) {
        if (this.nullMessage) {
            return null;
        }
        final StrSubstitutor sub = new StrSubstitutor((Map)values.getMessageValues(senderType), "%(", ")", '\\');
        String format = sub.replace(this.rawMessage);
        if (senderType != CommandSenderType.Player) {
            format = ChatColor.stripColor(format);
        }
        return format;
    }
    
    public String format(final CommandSenderType senderType, final Object... values) {
        return this.format(senderType, MessageValueMap.valueOf(values));
    }
    
    public String format(final CommandSender sender, final MessageValueMap values) {
        return this.format(CommandSenderType.valueOf(sender), values);
    }
    
    public String format(final CommandSender sender, final Object... values) {
        return this.format(sender, MessageValueMap.valueOf(values));
    }
    
    public final String getRawMessage() {
        return this.rawMessage;
    }
    
    public final boolean isNullMessage() {
        return this.nullMessage;
    }
    
    public String getMessage(final CommandSender sender, final MessageValueMap values) {
        return this.instance(sender, values).getMessage();
    }
    
    public String getMessage(final CommandSender sender, final Object... values) {
        return this.getMessage(sender, MessageValueMap.valueOf(values));
    }
    
    public ChatMessageInstance instance(final CommandSender sender, final MessageValueMap values) {
        return new ChatMessageInstance(this, sender, values);
    }
    
    public ChatMessageInstance instance(final CommandSender sender, final Object... values) {
        return this.instance(sender, MessageValueMap.valueOf(values));
    }
    
    public void send(final CommandSender sender, final MessageValueMap values) {
        new ChatMessageInstance(this, sender, values).send();
    }
    
    public void send(final CommandSender sender, final Object... values) {
        this.send(sender, MessageValueMap.valueOf(values));
    }
    
    @Override
    public String toString() {
        return this.rawMessage;
    }
    
    public FormatOption toFormatOption(final Object... values) {
        return this.toFormatOption(MessageValueMap.valueOf(values));
    }
    
    public FormatOption toFormatOption(final MessageValueMap values) {
        final Map<CommandSenderType, String> formatTypes = new HashMap<CommandSenderType, String>();
        for (final Map.Entry<CommandSenderType, Map<String, String>> entry : values.getMessageValues().entrySet()) {
            final CommandSenderType senderType = entry.getKey();
            formatTypes.put(senderType, this.format(senderType, values));
        }
        return new FormatOption(formatTypes);
    }
    
    public ChatMessage mergeWith(final ChatMessage other) {
        return new ChatMessage(this.getRawMessage() + "\n" + other.getRawMessage());
    }
    
    private String escapeCharacters(final String str) {
        if (str == null) {
            return null;
        }
        final char[] cArray = str.toCharArray();
        for (int i = 0; i < cArray.length; ++i) {
            final char c = cArray[i];
            if (c >= '\u0410' && c < '\u03ec') {
                final char[] array = cArray;
                final int n = i;
                array[n] -= '\u0350';
            }
            else if (c == '\u0451') {
                cArray[i] = '¸';
            }
        }
        return new String(cArray);
    }
    
    static {
        NONE = new ChatMessage(null);
    }
}
