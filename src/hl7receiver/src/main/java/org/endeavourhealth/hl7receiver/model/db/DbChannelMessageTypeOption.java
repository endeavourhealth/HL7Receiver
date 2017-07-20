package org.endeavourhealth.hl7receiver.model.db;

public class DbChannelMessageTypeOption {
    private int channelId;
    private String messageType;
    private DbMessageTypeOptionType messageTypeOptionType;
    private String messageTypeOptionValue;

    public int getChannelId() {
        return channelId;
    }

    public DbChannelMessageTypeOption setChannelId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getMessageType() {
        return messageType;
    }

    public DbChannelMessageTypeOption setMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public DbMessageTypeOptionType getMessageTypeOptionType() {
        return messageTypeOptionType;
    }

    public DbChannelMessageTypeOption setMessageTypeOptionType(DbMessageTypeOptionType messageTypeOptionType) {
        this.messageTypeOptionType = messageTypeOptionType;
        return this;
    }

    public String getMessageTypeOptionValue() {
        return messageTypeOptionValue;
    }

    public DbChannelMessageTypeOption setMessageTypeOptionValue(String messageTypeOptionValue) {
        this.messageTypeOptionValue = messageTypeOptionValue;
        return this;
    }
}