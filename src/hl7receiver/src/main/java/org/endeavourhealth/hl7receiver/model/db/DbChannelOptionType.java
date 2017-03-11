package org.endeavourhealth.hl7receiver.model.db;

import org.apache.commons.lang3.NotImplementedException;

public enum DbChannelOptionType {
    KEEP_ONLY_CURRENT_MESSAGE_PROCESSING_CONTENT_ATTEMPT("KeepOnlyCurrentMessageProcessingContentAttempt"),
    MAX_SKIPPABLE_PROCESSING_ERROR_MESSAGES("MaxSkippableProcessingErroredMessages"),
    SKIP_ONWARD_MESSAGE_SENDING_IN_PROCESSOR("SkipOnwardMessageSendingInProcessor");

    private String channelOptionType;

    DbChannelOptionType(String channelOptionType) {
        this.channelOptionType = channelOptionType;
    }

    public String getValue() {
        return this.channelOptionType;
    }

    public static DbChannelOptionType fromString(String value) {
        for (DbChannelOptionType dbChannelOption : DbChannelOptionType.values())
            if (dbChannelOption.getValue().equals(value))
                return dbChannelOption;

        throw new NotImplementedException("Option " + value + " not found");
    }
}
