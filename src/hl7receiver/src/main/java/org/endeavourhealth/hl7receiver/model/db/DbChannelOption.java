package org.endeavourhealth.hl7receiver.model.db;

public class DbChannelOption {
    private int channelId;
    private DbChannelOptionType channelOptionType;
    private String channelOptionValue;

    public int getChannelId() {
        return channelId;
    }

    public DbChannelOption setChannelId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    public DbChannelOptionType getChannelOptionType() {
        return channelOptionType;
    }

    public DbChannelOption setChannelOptionType(DbChannelOptionType channelOptionType) {
        this.channelOptionType = channelOptionType;
        return this;
    }

    public String getChannelOptionValue() {
        return channelOptionValue;
    }

    public DbChannelOption setChannelOptionValue(String channelOptionValue) {
        this.channelOptionValue = channelOptionValue;
        return this;
    }
}
