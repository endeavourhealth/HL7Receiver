package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.hl7parser.Helpers;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.EvnSegment;
import org.endeavourhealth.hl7parser.segments.MshSegment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.transform.MessageHeaderCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BartsMessageHeaderTransform extends ResourceTransformBase {

    public BartsMessageHeaderTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.MessageHeader;
    }

    public MessageHeader transform(AdtMessage sourceMessage) throws ParseException, MapperException, TransformException {
        Validate.notNull(sourceMessage);
        Validate.notNull(sourceMessage.getMshSegment());

        MshSegment mshSegment = sourceMessage.getMshSegment();

        MessageHeader target = new MessageHeader();

        setId(sourceMessage, target);
        setTimestamp(mshSegment, target);
        setEvent(mshSegment, target);
        setSource(mshSegment, target);
        setDestination(mshSegment, target);
        setResponsible(target);
        setMessageControlId(mshSegment, target);
        setSequenceNumber(mshSegment, target);
        setData(target);

        return target;
    }

    private void setId(AdtMessage source, MessageHeader target) throws MapperException {
        MessageHeaderCommon.setId(target, source.getMshSegment().getMessageControlId(), mapper);
    }

    private void setTimestamp(MshSegment source, MessageHeader target) {
        MessageHeaderCommon.setTimestamp(target, source.getDateTimeOfMessage().getLocalDateTime());
    }

    private void setEvent(MshSegment source, MessageHeader target) throws TransformException, MapperException {
        MessageHeaderCommon.setEvent(target, source.getMessageType(), source.getVersionId(), mapper);
    }

    private void setSource(MshSegment mshSegment, MessageHeader target) {
        MessageHeaderCommon.setSource(target, mshSegment.getSendingFacility(), mshSegment.getSendingApplication());
    }

    private void setDestination(MshSegment mshSegment, MessageHeader target) {
        MessageHeaderCommon.setDestination(target, mshSegment.getReceivingFacility(), mshSegment.getReceivingApplication());
    }

    private void setResponsible(MessageHeader target) throws TransformException {
        target.setResponsible(this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class));
    }

    private void setMessageControlId(MshSegment source, MessageHeader target) {
        MessageHeaderCommon.setMessageControlId(target, source.getMessageControlId());
    }

    private void setSequenceNumber(MshSegment source, MessageHeader target) throws ParseException {
        MessageHeaderCommon.setSequenceNumber(target, source.getSequenceNumber());
    }

    private void setData(MessageHeader target) {
        MessageHeaderCommon.setData(target, this.targetResources.getAllReferences());
    }
}
