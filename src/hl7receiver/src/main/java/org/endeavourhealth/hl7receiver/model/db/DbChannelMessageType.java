package org.endeavourhealth.hl7receiver.model.db;

import java.util.List;

public class DbChannelMessageType {
    private int channelId;
    private String messageType;
    private boolean isAllowed;
    private List<DbChannelMessageTypeOption> channelMessageTypeOptions;

    public int getChannelId() {
        return channelId;
    }

    public DbChannelMessageType setChannelId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getMessageType() {
        return messageType;
    }

    public DbChannelMessageType setMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public DbChannelMessageType setAllowed(boolean allowed) {
        isAllowed = allowed;
        return this;
    }

    public List<DbChannelMessageTypeOption> getChannelMessageTypeOptions() {
        return channelMessageTypeOptions;
    }

    public DbChannelMessageType setChannelMessageTypeOptions(List<DbChannelMessageTypeOption> channelMessageTypeOptions) {
        this.channelMessageTypeOptions = channelMessageTypeOptions;
        return this;
    }
}
