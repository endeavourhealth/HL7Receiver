package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.hl7transform.homerton.HomertonResourceContainer;
import org.endeavourhealth.hl7transform.homerton.transforms.constants.HomertonConstants;
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

public class MessageHeaderTransform extends HomertonTransformBase {

    public MessageHeaderTransform(Mapper mapper, HomertonResourceContainer targetResources) {
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

        setId(sourceMessage, target);

        LocalDateTime sourceMessageDateTime = source.getDateTimeOfMessage().getLocalDateTime();

        if (sourceMessageDateTime != null)
            target.setTimestamp(Helpers.toDate(sourceMessageDateTime));

        target.setEvent(new Coding()
                .setCode(source.getMessageType())
                .setDisplay(MessageTypeVs.getDescription(source.getMessageType()))
                .setVersion(source.getVersionId())
                .setSystem(FhirUri.CODE_SYSTEM_HL7V2_MESSAGE_TYPE));

        if (!HomertonConstants.sendingFacility.equals(source.getSendingFacility()))
            throw new TransformException("Sending facility of " + source.getSendingFacility() + " not recognised");

        target.getSource()
                .setName(source.getSendingFacility())
                .setSoftware(source.getSendingApplication());

        target.addDestination()
                .setName(source.getReceivingFacility())
                .addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_DESTINATION_SOFTWARE, source.getReceivingApplication()));

        target.setResponsible(this.targetResources.getHomertonOrganisationReference());

        target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.EXTENSION_HL7V2_MESSAGE_CONTROL_ID, source.getMessageControlId()));

        Integer sequenceNumber = source.getSequenceNumber();

        if (sequenceNumber != null)
            target.addExtension(ExtensionHelper.createIntegerExtension(FhirExtensionUri.EXTENSION_HL7V2_SEQUENCE_NUMBER, sequenceNumber));

        targetResources.addResource(target);
    }

    private void setId(AdtMessage source, MessageHeader target) throws MapperException {

        UUID id = mapper.mapMessageHeaderUuid(source.getMshSegment().getMessageControlId());
        target.setId(id.toString());
    }
}
