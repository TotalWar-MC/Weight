package com.steffbeard.totalwar.core.mechanics.armor.chat;

import org.bukkit.command.CommandSender;

import com.steffbeard.totalwar.core.mechanics.armor.chat.ChatMessage;

import org.bukkit.ChatColor;
import org.apache.commons.lang.text.StrSubstitutor;
import java.util.HashMap;
import java.util.Map;

public class FormattedChatMessage extends ChatMessage
{
    private final String defaultMessage;
    private final MessageValueMap formatOptions;
    private final Map<CommandSenderType, String> formatted;
    
    public FormattedChatMessage(final String rawMessage, final String defaultRawMessage, final Object... formatOptions) {
        this(rawMessage, defaultRawMessage, MessageValueMap.valueOf(formatOptions));
    }
    
    public FormattedChatMessage(final String rawMessage, final String defaultRawMessage, final MessageValueMap formatOptions) {
        super(rawMessage);
        this.formatOptions = formatOptions;
        this.formatted = new HashMap<CommandSenderType, String>();
        if (this.isNullMessage()) {
            for (final CommandSenderType type : CommandSenderType.values()) {
                this.formatted.put(type, null);
            }
            this.defaultMessage = null;
            return;
        }
        this.defaultMessage = this.getFormattedMessage(defaultRawMessage, formatOptions.getMessageValues(CommandSenderType.Console));
        final Map<CommandSenderType, Map<String, String>> values = formatOptions.getMessageValues();
        for (final Map.Entry<CommandSenderType, Map<String, String>> entry : values.entrySet()) {
            final CommandSenderType type = entry.getKey();
            final Map<String, String> formats = entry.getValue();
            this.formatted.put(type, this.getFormattedMessage(rawMessage, formats));
        }
    }
    
    private String getFormattedMessage(final String rawMessage, final Map<String, String> formats) {
        final StrSubstitutor sub = new StrSubstitutor((Map)formats, "&(", ")", '\\');
        String format = sub.replace(rawMessage);
        format = ChatColor.translateAlternateColorCodes('&', format);
        format = format.replaceAll("[ ]{2,}", " ");
        return format;
    }
    
    @Override
    public String format(final CommandSenderType senderType, final MessageValueMap values) {
        if (this.isNullMessage()) {
            return null;
        }
        final StrSubstitutor sub = new StrSubstitutor((Map)values.getMessageValues(senderType), "%(", ")", '\\');
        String format = sub.replace(this.getRawMessage(senderType));
        if (senderType != CommandSenderType.Player) {
            format = ChatColor.stripColor(format);
        }
        return format;
    }
    
    public final String getRawMessage(final CommandSenderType senderType) {
        return (senderType == CommandSenderType.Console) ? this.defaultMessage : this.formatted.get(senderType);
    }
    
    public final String getRawMessage(final CommandSender sender) {
        return this.getRawMessage(CommandSenderType.valueOf(sender));
    }
    
    public final String getRawMessageDefault() {
        return this.defaultMessage;
    }
    
    public final MessageValueMap getFormatOptions() {
        return this.formatOptions;
    }
    
    @Override
    public FormattedChatMessage mergeWith(final ChatMessage other) {
        final String raw = this.getRawMessage() + "\n" + other.getRawMessage();
        String rawDefault = this.getRawMessageDefault() + "\n" + other.getRawMessage();
        final MessageValueMap formatOptions = (MessageValueMap)this.getFormatOptions().clone();
        if (other instanceof FormattedChatMessage) {
            final FormattedChatMessage otherF = (FormattedChatMessage)other;
            formatOptions.putAll(otherF.getFormatOptions());
            rawDefault = this.getRawMessageDefault() + "\n" + otherF.getRawMessageDefault();
        }
        return new FormattedChatMessage(raw, rawDefault, formatOptions);
    }
}
