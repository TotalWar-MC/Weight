package com.steffbeard.totalwar.core.mechanics.armor.chat;

import org.bukkit.command.CommandSender;

import com.steffbeard.totalwar.core.mechanics.armor.chat.ChatMessage;

public final class ChatMessageInstance
{
    private final ChatMessage type;
    private final CommandSender sender;
    private final MessageValueMap values;
    private final String message;
    
    public ChatMessageInstance(final ChatMessage type, final CommandSender sender, final Object... values) {
        this(type, sender, MessageValueMap.valueOf(values));
    }
    
    public ChatMessageInstance(final ChatMessage type, final CommandSender sender, final MessageValueMap values) {
        this.type = type;
        this.sender = sender;
        this.values = values;
        this.message = type.format(sender, values);
    }
    
    @Override
    public String toString() {
        return this.message;
    }
    
    public final ChatMessage getType() {
        return this.type;
    }
    
    public final CommandSender getSender() {
        return this.sender;
    }
    
    public final MessageValueMap getValues() {
        return this.values;
    }
    
    public final String getMessage() {
        return this.message;
    }
    
    public final boolean hasMessage() {
        return this.message != null;
    }
    
    public void send() {
        if (this.hasMessage()) {
            this.sender.sendMessage(this.message);
        }
    }
}
