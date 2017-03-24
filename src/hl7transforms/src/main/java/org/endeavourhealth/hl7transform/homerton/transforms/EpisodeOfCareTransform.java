package org.endeavourhealth.hl7transform.homerton.transforms;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

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

        mapAndSetId(getUniqueIdentifyingString(sourceMessage), target);

        setIdentifiers(sourceMessage, target);

        // set status

        setPatient(targetResources.getPatient(), target);

        // set managing organisation

        // period


        targetResources.addResource(target);
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

    protected static String getUniqueIdentifyingString(AdtMessage sourceMessage) throws TransformException {

        final String episodeAssigningAuthority = "Homerton FIN";

        Cx alternateVisitId = sourceMessage.getPv1Segment().getAlternateVisitID();

        Validate.notNull(alternateVisitId, "PV1.getAlternateVisitID()");
        Validate.notBlank(alternateVisitId.getAssigningAuthority(), "PV1.getAlternateVisitID().getAssigningAuthority()");
        Validate.notBlank(alternateVisitId.getId(), "PV1.getAlternateVisitID().getId()");

        if (!episodeAssigningAuthority.equals(alternateVisitId.getAssigningAuthority()))
            throw new TransformException("Episode number assigning authority " + alternateVisitId.getAssigningAuthority() + " not recognised");

        return createIdentifyingString(PatientTransform.getUniqueIdentifyingString(sourceMessage),
                ImmutableMap.of(
                        "EpisodeIdentifierAssigningAuthority", alternateVisitId.getAssigningAuthority(),
                        "EpisodeIdentifierValue", alternateVisitId.getId()
                ));
    }
}
