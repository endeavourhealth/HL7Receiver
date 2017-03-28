package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.homerton.transforms.valuesets.MessageTypeVs;
import org.endeavourhealth.hl7transform.mapper.MapperException;
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

public class MessageHeaderTransform extends ResourceTransformBase {

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

        MshSegment mshSegment = sourceMessage.getMshSegment();
        MessageHeader target = new MessageHeader();

        setId(sourceMessage, target);
        setTimestamp(mshSegment, target);
        setEvent(mshSegment, target);
        setSource(mshSegment, target);
        setDestination(mshSegment, target);
        setResponsible(mshSegment, target);
        setMessageControlId(mshSegment, target);
        setSequenceNumber(mshSegment, target);

        targetResources.addResource(target);
    }

    private void setTimestamp(MshSegment source, MessageHeader target) {
        LocalDateTime sourceMessageDateTime = source.getDateTimeOfMessage().getLocalDateTime();

        if (sourceMessageDateTime != null)
            target.setTimestamp(Helpers.toDate(sourceMessageDateTime));
    }

    private void setEvent(MshSegment source, MessageHeader target) {
        target.setEvent(new Coding()
                .setCode(source.getMessageType())
                .setDisplay(MessageTypeVs.getDescription(source.getMessageType()))
                .setVersion(source.getVersionId())
                .setSystem(FhirUri.CODE_SYSTEM_HL7V2_MESSAGE_TYPE));
    }

    private void setSource(MshSegment mshSegment, MessageHeader target) {
        target.setSource(new MessageHeader.MessageSourceComponent()
                .setName(mshSegment.getSendingFacility())
                .setSoftware(mshSegment.getSendingApplication()));
    }

    private void setDestination(MshSegment mshSegment, MessageHeader target) {
        target.addDestination()
                .setName(mshSegment.getReceivingFacility())
                .addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_DESTINATION_SOFTWARE, mshSegment.getReceivingApplication()));
    }

    private void setResponsible(MshSegment mshSegment, MessageHeader target) throws TransformException {
        target.setResponsible(this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class));

    }

    private void setMessageControlId(MshSegment source, MessageHeader target) {
        target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_MESSAGE_CONTROL_ID, source.getMessageControlId()));
    }

    private void setSequenceNumber(MshSegment source, MessageHeader target) throws ParseException {
        Integer sequenceNumber = source.getSequenceNumber();

        if (sequenceNumber != null)
            target.addExtension(ExtensionHelper.createIntegerExtension(FhirExtensionUri.EXTENSION_HL7V2_SEQUENCE_NUMBER, sequenceNumber));
    }

    private void setId(AdtMessage source, MessageHeader target) throws MapperException {

        UUID id = mapper.mapMessageHeaderUuid(source.getMshSegment().getMessageControlId());
        target.setId(id.toString());
    }
}
