package org.endeavourhealth.hl7transform.transforms.homerton.transforms;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.hl7parser.segments.EvnSegment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.transform.MessageHeaderCommon;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.MshSegment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.UUID;

public class HomertonMessageHeaderTransform extends ResourceTransformBase {

    public HomertonMessageHeaderTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.MessageHeader;
    }

    public MessageHeader transform(AdtMessage sourceMessage) throws ParseException, MapperException, TransformException {
        Validate.notNull(sourceMessage);
        Validate.notNull(sourceMessage.getMshSegment());
        Validate.notNull(sourceMessage.getEvnSegment());

        MshSegment mshSegment = sourceMessage.getMshSegment();
        EvnSegment evnSegment = sourceMessage.getEvnSegment();

        MessageHeader target = new MessageHeader();

        setId(sourceMessage, target);
        setTimestamp(mshSegment, target);
        setEvent(mshSegment, target);
        setSource(mshSegment, target);
        setDestination(mshSegment, target);
        setResponsible(target);
        setMessageControlId(mshSegment, target);
        setSequenceNumber(mshSegment, target);
        setEnterer(evnSegment, target);
        setData(target);

        return target;
    }

    private void setTimestamp(MshSegment source, MessageHeader target) {
        MessageHeaderCommon.setTimestamp(target, source.getDateTimeOfMessage().getLocalDateTime());
    }

    private void setEvent(MshSegment source, MessageHeader target) throws MapperException, TransformException {

        CodeableConcept messageType = mapper.getCodeMapper().mapMessageType(source.getMessageType());

        Coding messageTypeCoding = CodeableConceptHelper.getFirstCoding(messageType);

        if (messageTypeCoding == null)
            throw new TransformException("Could not map message type");

        target.setEvent(new Coding()
                .setCode(messageTypeCoding.getCode())
                .setDisplay(messageTypeCoding.getDisplay())
                .setVersion(source.getVersionId())
                .setSystem(messageTypeCoding.getSystem()));
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

    private void setEnterer(EvnSegment evnSegment, MessageHeader target) throws TransformException, MapperException, ParseException {

        if (evnSegment.getOperators() == null)
            return;

        HomertonPractitionerTransform homertonPractitionerTransform = new HomertonPractitionerTransform(mapper, targetResources);
        List<Reference> references = homertonPractitionerTransform.createPractitioners(evnSegment.getOperators());

        if (references.size() > 1)
            throw new TransformException("More than one entering user found");

        if (references.size() == 1)
            target.setEnterer(references.get(0));
    }

    private void setData(MessageHeader target) {
        MessageHeaderCommon.setData(target, this.targetResources.getAllReferences());
    }

    private void setId(AdtMessage source, MessageHeader target) throws MapperException {

        UUID id = mapper.getResourceMapper().mapMessageHeaderUuid(source.getMshSegment().getMessageControlId());
        target.setId(id.toString());
    }
}
