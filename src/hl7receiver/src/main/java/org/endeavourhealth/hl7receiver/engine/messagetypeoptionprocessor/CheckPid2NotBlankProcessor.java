package org.endeavourhealth.hl7receiver.engine.messagetypeoptionprocessor;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7receiver.engine.HL7KeyFields;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.db.DbChannelMessageTypeOption;
import org.endeavourhealth.hl7receiver.model.exceptions.MessageProcessingException;

public class CheckPid2NotBlankProcessor extends MessageTypeOptionProcessor {

    @Override
    public void process(DbChannel dbChannel, DbChannelMessageTypeOption dbChannelMessageTypeOption, HL7KeyFields hl7KeyFields) throws MessageProcessingException {

        if (dbChannel.getPid2Field() == null)
            return;

        if (StringUtils.isNotBlank(hl7KeyFields.getPid2()))
            return;

        String exceptionMessage = "Patient identifier could not be found in PID." + dbChannel.getPid2Field().toString();

        if (StringUtils.isNotBlank(dbChannel.getPid2AssigningAuthority()))
            exceptionMessage += " with assigning authority '" + dbChannel.getPid2AssigningAuthority() + "'";

        throw new MessageProcessingException(exceptionMessage);
    }
}
