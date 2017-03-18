package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class EpisodeOfCareTransform {

    private Mapper mapper;
    private ResourceContainer targetResources;

    public EpisodeOfCareTransform(Mapper mapper, ResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public void transform(AdtMessage sourceMessage) throws ParseException, TransformException, MapperException {

        if (!sourceMessage.hasPv1Segment())
            return;

        EpisodeOfCare target = new EpisodeOfCare();

        setIdentifiers(sourceMessage, target);

        // set status

        setPatient(target, targetResources.getPatient());

        // set managing organisation

        // period

        target.setId(UUID.randomUUID().toString());

        targetResources.addResource(target);
    }

    private static void setIdentifiers(AdtMessage source, EpisodeOfCare target) {

        Identifier visitNumber = transformIdentifier(source.getPv1Segment().getVisitNumber(), source);

        if (visitNumber != null)
            target.addIdentifier(visitNumber);

        Identifier alternateVisitId = transformIdentifier(source.getPv1Segment().getAlternateVisitID(), source);

        if (alternateVisitId != null)
            target.addIdentifier(alternateVisitId);
    }

    private static Identifier transformIdentifier(Cx cx, AdtMessage source) {
        if (cx == null)
            return null;

        if (StringUtils.isBlank(cx.getId()))
            return null;

        if (StringUtils.isBlank(cx.getAssigningAuthority()) && StringUtils.isBlank(cx.getIdentifierTypeCode()))
            return null;

        String identifierSystem = FhirUri.getHl7v2LocalEncounterIdentifierSystem(
                source.getMshSegment().getSendingFacility(),
                cx.getAssigningAuthority(),
                cx.getIdentifierTypeCode());

        return new Identifier()
                .setSystem(identifierSystem)
                .setValue(cx.getId());
    }

    private static void setPatient(EpisodeOfCare target, Patient patient) {
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patient.getId()));
    }
}
