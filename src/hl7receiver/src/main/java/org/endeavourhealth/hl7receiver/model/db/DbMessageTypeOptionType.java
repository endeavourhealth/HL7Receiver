package org.endeavourhealth.hl7receiver.model.db;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

public enum DbMessageTypeOptionType {
    CHECK_PID1_NOT_BLANK_AT_MESSAGE_RECEIPT("CheckPid1NotBlank"),
    CHECK_PID2_NOT_BLANK_AT_MESSAGE_RECEIPT("CheckPid2NotBlank"),
    CHECK_MRG_SEGMENT_FIELD_5_NOT_BLANK("CheckMrgSegmentField5NotBlank");

    private String messageTypeOptionType;

    DbMessageTypeOptionType(String messageTypeOptionType) {
        this.messageTypeOptionType = messageTypeOptionType;
    }

    public String getValue() {
        return this.messageTypeOptionType;
    }

    public static DbMessageTypeOptionType fromString(String value) {
        for (DbMessageTypeOptionType messageTypeOptionType : DbMessageTypeOptionType.values())
            if (messageTypeOptionType.getValue().equals(value))
                return messageTypeOptionType;

        throw new NotImplementedException("Option " + value + " not found");
    }

    public static boolean isMessageTypeOptionTypeEnabled(String messageTypeOptionValue) {
        return "enabled".equalsIgnoreCase(StringUtils.trim(messageTypeOptionValue));
    }
}

