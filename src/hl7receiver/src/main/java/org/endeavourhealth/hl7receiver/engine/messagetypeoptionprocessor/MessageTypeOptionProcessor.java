package org.endeavourhealth.hl7receiver.engine.messagetypeoptionprocessor;

import org.endeavourhealth.hl7receiver.engine.HL7KeyFields;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.db.DbChannelMessageTypeOption;
import org.endeavourhealth.hl7receiver.model.exceptions.MessageProcessingException;

public abstract class MessageTypeOptionProcessor {

    public abstract void process(DbChannel dbChannel, DbChannelMessageTypeOption dbChannelMessageTypeOption, HL7KeyFields hl7KeyFields) throws MessageProcessingException;

    private static final CheckPid1NotBlankProcessor checkPid1NotBlankProcessor = new CheckPid1NotBlankProcessor();
    private static final CheckPid2NotBlankProcessor checkPid2NotBlankProcessor = new CheckPid2NotBlankProcessor();
    private static final CheckMrgSegmentField5NotBlankProcessor checkMrgSegmentField5NotBlankProcessor = new CheckMrgSegmentField5NotBlankProcessor();

    public static MessageTypeOptionProcessor create(DbChannelMessageTypeOption channelMessageTypeOption) {

        switch (channelMessageTypeOption.getMessageTypeOptionType()) {
            case CHECK_PID1_NOT_BLANK_AT_MESSAGE_RECEIPT: return checkPid1NotBlankProcessor;
            case CHECK_PID2_NOT_BLANK_AT_MESSAGE_RECEIPT: return checkPid2NotBlankProcessor;
            case CHECK_MRG_SEGMENT_FIELD_5_NOT_BLANK: return checkMrgSegmentField5NotBlankProcessor;
            default: return null;
        }
    }
}
