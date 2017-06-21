package org.endeavourhealth.hl7transform.common.transform;

import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.hl7parser.Helpers;
import org.endeavourhealth.hl7parser.ParseException;
import org.hl7.fhir.instance.model.*;

import java.time.LocalDateTime;
import java.util.List;

public abstract class MessageHeaderCommon {

    public static void setTimestamp(MessageHeader target, LocalDateTime sourceMessageDateTime) {
        if (sourceMessageDateTime != null)
            target.setTimestamp(Helpers.toDate(sourceMessageDateTime));
    }

    public static void setSource(MessageHeader target, String sendingFacility, String sendingApplication) {
        target.setSource(new MessageHeader.MessageSourceComponent()
                .setName(sendingFacility)
                .setSoftware(sendingApplication));
    }

    public static void setDestination(MessageHeader target, String receivingFacility, String receivingApplication) {
        target.addDestination()
                .setName(receivingFacility)
                .addExtension(ExtensionConverter.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_DESTINATION_SOFTWARE, receivingApplication));
    }

    public static void setMessageControlId(MessageHeader target, String messageControlId) {
        target.addExtension(ExtensionConverter.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_MESSAGE_CONTROL_ID, messageControlId));
    }

    public static void setSequenceNumber(MessageHeader target, Integer sequenceNumber) throws ParseException {
        if (sequenceNumber != null)
            target.addExtension(ExtensionConverter.createIntegerExtension(FhirExtensionUri.EXTENSION_HL7V2_SEQUENCE_NUMBER, sequenceNumber));
    }

    public static void setData(MessageHeader target, List<Reference> references) {
        for (Reference reference : references)
            target.addData(reference);
    }
}
