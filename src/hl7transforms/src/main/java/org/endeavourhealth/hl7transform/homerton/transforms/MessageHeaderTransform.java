package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7parser.Helpers;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.MshSegment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.ExtensionHelper;
import org.hl7.fhir.instance.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class MessageHeaderTransform extends TransformBase {

    public MessageHeaderTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.MessageHeader;
    }

    public void transform(AdtMessage sourceMessage) throws ParseException, MapperException, TransformException {
        Validate.notNull(sourceMessage);
        Validate.notNull(sourceMessage.getMshSegment());

        MshSegment source = sourceMessage.getMshSegment();
        MessageHeader target = new MessageHeader();

        UUID messageId = getId(sourceMessage);
        target.setId(messageId.toString());

        LocalDateTime sourceMessageDateTime = source.getDateTimeOfMessage().getLocalDateTime();

        if (sourceMessageDateTime != null)
            target.setTimestamp(Helpers.toDate(sourceMessageDateTime));

        target.setEvent(new Coding()
                .setCode(source.getMessageType())
                .setDisplay(getMessageTypeDescription(source.getMessageType()))
                .setVersion(source.getVersionId())
                .setSystem(FhirUri.CODE_SYSTEM_HL7V2_MESSAGE_TYPE));

        target.getSource()
                .setName(source.getSendingFacility())
                .setSoftware(source.getSendingApplication());

        target.addDestination()
                .setName(source.getReceivingFacility())
                .addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_DESTINATION_SOFTWARE, source.getReceivingApplication()));

        target.setResponsible(this.targetResources.getManagingOrganisation());

        target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_MESSAGE_CONTROL_ID, source.getMessageControlId()));

        Integer sequenceNumber = source.getSequenceNumber();

        if (sequenceNumber != null)
            target.addExtension(ExtensionHelper.createIntegerExtension(FhirExtensionUri.EXTENSION_HL7V2_SEQUENCE_NUMBER, sequenceNumber));

        targetResources.addResource(target);
    }

    private UUID getId(AdtMessage source) throws MapperException, TransformException {
        String uniqueIdentifyingString = getUniqueMessageHeaderString(source);

        return mapper.mapResourceUuid(ResourceType.MessageHeader, uniqueIdentifyingString);
    }

    private static String getUniqueMessageHeaderString(AdtMessage source) throws TransformException {

        if (StringUtils.isBlank(source.getMshSegment().getMessageControlId()))
            throw new TransformException("Cannot create unique resource identifying string as MessageControlId is blank");

        return StringUtils.deleteWhitespace(source.getMshSegment().getMessageControlId());
    }

    private static String getMessageTypeDescription(String messageType) {
        switch (messageType) {
            case "ADT^A01": return "Admit / visit notification";
            case "ADT^A02": return "Transfer a patient";
            case "ADT^A03": return "Discharge/end visit";
            case "ADT^A04": return "Register a patient";
            case "ADT^A05": return "Pre-admit a patient";
            case "ADT^A06": return "Change an outpatient to an inpatient";
            case "ADT^A07": return "Change an inpatient to an outpatient";
            case "ADT^A08": return "Update patient information";
            case "ADT^A09": return "Patient departing - tracking";
            case "ADT^A10": return "Patient arriving - tracking";
            case "ADT^A11": return "Cancel admit/visit notification";
            case "ADT^A12": return "Cancel transfer";
            case "ADT^A13": return "Cancel discharge/end visit";
            case "ADT^A14": return "Pending admit";
            case "ADT^A15": return "Pending transfer";
            case "ADT^A16": return "Pending discharge";
            case "ADT^A17": return "Swap patients";
            case "ADT^A18": return "Merge patient information";
            case "ADT^A19": return "Patient query";
            case "ADT^A20": return "Bed status update";
            case "ADT^A21": return "Patient goes on a leave of absence";
            case "ADT^A22": return "Patient returns from a leave of absence";
            case "ADT^A23": return "Delete a patient record";
            case "ADT^A24": return "Link patient information";
            case "ADT^A25": return "Cancel pending discharge";
            case "ADT^A26": return "Cancel pending transfer";
            case "ADT^A27": return "Cancel pending admit";
            case "ADT^A28": return "Add person information";
            case "ADT^A29": return "Delete person information";
            case "ADT^A30": return "Merge person information";
            case "ADT^A31": return "Update person information";
            case "ADT^A32": return "Cancel patient arriving - tracking";
            case "ADT^A33": return "Cancel patient departing - tracking";
            case "ADT^A34": return "Merge patient information - patient ID only";
            case "ADT^A35": return "Merge patient information - account number only";
            case "ADT^A36": return "Merge patient information - patient ID and account number";
            case "ADT^A37": return "Unlink patient information";
            case "ADT^A38": return "Cancel pre-admit";
            case "ADT^A39": return "Merge person - external ID";
            case "ADT^A40": return "Merge patient - internal ID";
            case "ADT^A41": return "Merge account - patient account number";
            case "ADT^A42": return "Merge visit - visit number";
            case "ADT^A43": return "Move patient information - internal ID";
            case "ADT^A44": return "Move account information - patient account number";
            case "ADT^A45": return "Move visit information - visit number";
            case "ADT^A46": return "Change external ID";
            case "ADT^A47": return "Change internal ID";
            case "ADT^A48": return "Change alternate patient ID";
            case "ADT^A49": return "Change patient account number";
            case "ADT^A50": return "Change visit number";
            case "ADT^A51": return "Change alternate visit ID";
            default: throw new NotImplementedException("Message type " + messageType + " not recognised");
        }
    }
}
