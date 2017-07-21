package org.endeavourhealth.hl7receiver.engine.messagetypeoptionprocessor;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.Message;
import org.endeavourhealth.hl7parser.segments.MrgSegment;
import org.endeavourhealth.hl7parser.segments.SegmentName;
import org.endeavourhealth.hl7receiver.engine.HL7KeyFields;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.db.DbChannelMessageTypeOption;
import org.endeavourhealth.hl7receiver.model.exceptions.MessageProcessingException;

public class CheckMrgSegmentField5NotBlankProcessor extends MessageTypeOptionProcessor {

    @Override
    public void process(DbChannel dbChannel, DbChannelMessageTypeOption dbChannelMessageTypeOption, HL7KeyFields hl7KeyFields) throws MessageProcessingException {

        Message message = hl7KeyFields.getMessage();

        MrgSegment mrgSegment = message.getSegment(SegmentName.MRG, MrgSegment.class);

        if (mrgSegment == null)
            throw new MessageProcessingException("MRG segment not present");

        if (mrgSegment.getPriorVisitNumber() == null)
            throw new MessageProcessingException("MRG.5 is blank");

        if (mrgSegment.getPriorVisitNumber().allComponentsAreBlank())
            throw new MessageProcessingException("MRG.5 is blank");

        if (StringUtils.isBlank(mrgSegment.getPriorVisitNumber().getId()))
            throw new MessageProcessingException("MRG.5.1 is blank");
    }
}
