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
import org.hl7.fhir.instance.model.*;

import java.util.UUID;

public class EpisodeOfCareTransform extends TransformBase {
    private static final String encounterAssigningAuthority = "Homerton FIN";

    public EpisodeOfCareTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.EpisodeOfCare;
    }

    public void transform(AdtMessage sourceMessage) throws TransformException, MapperException, ParseException {

        if (!sourceMessage.hasPv1Segment())
            return;

        EpisodeOfCare target = new EpisodeOfCare();

        mapAndSetId(getUniqueIdentifyingString(sourceMessage), target);

        setIdentifiers(sourceMessage, target);

        // set status

        setPatient(targetResources.getPatient(), target);

        // set managing organisation

        // period


        targetResources.addResource(target);
    }

    protected static String getUniqueIdentifyingString(AdtMessage sourceMessage) throws TransformException {
        Cx cx = sourceMessage.getPidSegment().getPatientAccountNumber();

        if (cx == null)
            throw new TransformException("Episode number (PID.18) is null");

        if (StringUtils.isEmpty(cx.getAssigningAuthority()))
            throw new TransformException("Episode number (PID.18) assigning authority is empty");

        if (StringUtils.isEmpty(cx.getId()))
            throw new TransformException("Episode number (PID.18) id is empty");

        if (!encounterAssigningAuthority.equals(cx.getAssigningAuthority()))
            throw new TransformException("Episode number assigning authority does not match");

        return StringUtils.deleteWhitespace(
                PatientTransform.getUniqueIdentifyingString(sourceMessage)
                        + "-" + cx.getAssigningAuthority() + "-" + cx.getId());
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

    private static void setPatient(Patient patient, EpisodeOfCare target) {
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patient.getId()));
    }
}
