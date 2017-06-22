package org.endeavourhealth.hl7transform.common.transform;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.hl7parser.Helpers;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.MshSegment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public abstract class MessageHeaderCommon {

    public static void setId(MessageHeader target, String messageControlId, Mapper mapper) throws MapperException {
        UUID id = mapper.getResourceMapper().mapMessageHeaderUuid(messageControlId);
        target.setId(id.toString());
    }

    public static void setTimestamp(MessageHeader target, LocalDateTime sourceMessageDateTime) {
        if (sourceMessageDateTime != null)
            target.setTimestamp(Helpers.toDate(sourceMessageDateTime));
    }

    public static void setEvent(MessageHeader target, String messageType, String hl7Version, Mapper mapper) throws MapperException, TransformException {
        CodeableConcept messageTypeCode = mapper.getCodeMapper().mapMessageType(messageType);

        Coding messageTypeCoding = CodeableConceptHelper.getFirstCoding(messageTypeCode);

        if (messageTypeCoding == null)
            throw new TransformException("Could not map message type");

        target.setEvent(new Coding()
                .setCode(messageTypeCoding.getCode())
                .setDisplay(messageTypeCoding.getDisplay())
                .setVersion(hl7Version)
                .setSystem(messageTypeCoding.getSystem()));
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
