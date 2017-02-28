package org.endeavourhealth.transform.hl7v2.transform;

import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.hl7v2.parser.Helpers;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.segments.MshSegment;
import org.endeavourhealth.transform.hl7v2.transform.converters.ExtensionHelper;
import org.endeavourhealth.transform.hl7v2.transform.converters.IdentifierHelper;
import org.hl7.fhir.instance.model.*;

import java.time.LocalDateTime;

public class MessageHeaderTransform {
    public static MessageHeader fromHl7v2(MshSegment source) throws ParseException {
        MessageHeader target = new MessageHeader();

        LocalDateTime sourceMessageDateTime = source.getDateTimeOfMessage();
        target.setIdElement(new IdType().setValue(IdentifierHelper.generateId(sourceMessageDateTime)));

        if (sourceMessageDateTime != null)
            target.setTimestamp(Helpers.toDate(sourceMessageDateTime));

        target.setEvent(new Coding()
                .setCode(source.getMessageType())
                .setVersion(source.getVersionId())
                .setSystem(FhirUri.CODE_SYSTEM_HL7V2_MESSAGE_TYPE));

        target.getSource()
                .setName(source.getSendingFacility())
                .setSoftware(source.getSendingApplication());

        target.addDestination()
                .setName(source.getReceivingFacility())
                .addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_DESTINATION_SOFTWARE, source.getReceivingApplication()));

        target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_MESSAGE_CONTROL_ID, source.getMessageControlId()));

        Integer sequenceNumber = source.getSequenceNumber();

        if (sequenceNumber != null)
            target.addExtension(ExtensionHelper.createIntegerExtension(FhirExtensionUri.EXTENSION_HL7V2_SEQUENCE_NUMBER, sequenceNumber));

        return target;
    }
}
