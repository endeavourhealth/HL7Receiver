package org.endeavourhealth.hl7receiver.engine.messagetypeoptionprocessor;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7receiver.engine.HL7KeyFields;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.db.DbChannelMessageTypeOption;
import org.endeavourhealth.hl7receiver.model.exceptions.MessageProcessingException;

public class CheckPid1NotBlankProcessor extends MessageTypeOptionProcessor {

    @Override
    public void process(DbChannel dbChannel, DbChannelMessageTypeOption dbChannelMessageTypeOption, HL7KeyFields hl7KeyFields) throws MessageProcessingException {

        if (dbChannel.getPid1Field() == null)
            return;

        if (StringUtils.isNotBlank(hl7KeyFields.getPid1()))
            return;

        String exceptionMessage = "Patient identifier could not be found in PID." + dbChannel.getPid1Field().toString();

        if (StringUtils.isNotBlank(dbChannel.getPid1AssigningAuthority()))
            exceptionMessage += " with assigning authority '" + dbChannel.getPid1AssigningAuthority() + "'";

        throw new MessageProcessingException(exceptionMessage);
    }
}
