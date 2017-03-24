package org.endeavourhealth.hl7transform.homerton.transforms;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EpisodeOfCareTransform extends TransformBase {

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

        setId(sourceMessage, target);

        setIdentifiers(sourceMessage, target);

        // set status

        setPatient(targetResources.getPatient(), target);

        // set managing organisation

        // period


        targetResources.addResource(target);
    }

    protected void setId(AdtMessage source, EpisodeOfCare target) throws TransformException, MapperException {

        String patientIdentifierValue = PatientTransform.getPatientIdentifierValue(source, HomertonConstants.primaryPatientIdentifierTypeCode);
        String episodeIdentifierValue = getEpisodeIdentifierValue(source, HomertonConstants.primaryEpisodeIdentifierAssigningAuthority);
        UUID episodeUuid = mapper.mapEpisodeUuid(HomertonConstants.primaryPatientIdentifierTypeCode, patientIdentifierValue, HomertonConstants.primaryEpisodeIdentifierAssigningAuthority, episodeIdentifierValue);

        target.setId(episodeUuid.toString());
    }

    private void setIdentifiers(AdtMessage source, EpisodeOfCare target) throws TransformException {

        Identifier visitNumber = IdentifierConverter.createIdentifier(source.getPv1Segment().getVisitNumber(), getResourceType());

        if (visitNumber != null)
            target.addIdentifier(visitNumber);

        Identifier alternateVisitId = IdentifierConverter.createIdentifier(source.getPv1Segment().getAlternateVisitID(), getResourceType());

        if (alternateVisitId != null)
            target.addIdentifier(alternateVisitId);
    }

    private static void setPatient(Patient patient, EpisodeOfCare target) {
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patient.getId()));
    }

    public static List<Cx> getAllEpisodeIdentifiers(AdtMessage source) {
        List<Cx> episodeIdentifiers = new ArrayList<>();

        if (source.getPv1Segment().getVisitNumber() != null)
            episodeIdentifiers.add(source.getPv1Segment().getVisitNumber());

        if (source.getPv1Segment().getAlternateVisitID() != null)
            episodeIdentifiers.add(source.getPv1Segment().getAlternateVisitID());

        return episodeIdentifiers;
    }

    public static String getEpisodeIdentifierValue(AdtMessage source, String episodeIdentifierAssigningAuthority) {
        return getAllEpisodeIdentifiers(source)
                .stream()
                .filter(t -> episodeIdentifierAssigningAuthority.equals(t.getAssigningAuthority()))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());
    }
}
