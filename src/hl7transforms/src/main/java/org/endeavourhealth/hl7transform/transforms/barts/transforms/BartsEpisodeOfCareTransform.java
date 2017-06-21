package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.DateTimeHelper;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.common.transform.EpisodeOfCareCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class BartsEpisodeOfCareTransform extends ResourceTransformBase {

    private static final Logger LOG = LoggerFactory.getLogger(BartsEpisodeOfCareTransform.class);

    public BartsEpisodeOfCareTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.EpisodeOfCare;
    }

    public EpisodeOfCare transform(AdtMessage source) throws TransformException, MapperException, ParseException {

        if (!source.hasPv1Segment())
            return null;

        EpisodeOfCare target = new EpisodeOfCare();

        setId(source, target);

        setIdentifiers(source, target);

        // set status

        setPatient(target);

        setManagingOrganisation(source, target);

        setPeriod(source, target);

        return target;
    }

    protected void setId(AdtMessage source, EpisodeOfCare target) throws TransformException, MapperException {

        String patientIdentifierValue = BartsPatientTransform.getBartsPrimaryPatientIdentifierValue(source);
        String episodeIdentifierValue = getBartsPrimaryEpisodeIdentifierValue(source);

        UUID episodeUuid = mapper.getResourceMapper().mapEpisodeUuid(
                null,
                BartsConstants.primaryPatientIdentifierAssigningAuthority,
                patientIdentifierValue,
                BartsConstants.primaryEpisodeIdentifierTypeCode,
                null,
                episodeIdentifierValue);

        target.setId(episodeUuid.toString());
    }

    public static String getBartsPrimaryEpisodeIdentifierValue(AdtMessage source) {
        return EpisodeOfCareCommon.getEpisodeIdentifierValueByTypeCode(source, BartsConstants.primaryEpisodeIdentifierTypeCode);
    }

    private void setIdentifiers(AdtMessage source, EpisodeOfCare target) throws TransformException, MapperException {
        List<Cx> cxs = EpisodeOfCareCommon.getAllEpisodeIdentifiers(source);

        for (Cx cx : cxs) {
            Identifier episodeIdentifier = IdentifierConverter.createIdentifier(cx, getResourceType(), mapper);

            if (episodeIdentifier != null)
                target.addIdentifier(episodeIdentifier);
        }
    }

    private void setPatient(EpisodeOfCare target) throws TransformException {
        target.setPatient(targetResources.getResourceReference(ResourceTag.PatientSubject, Patient.class));
    }

    private void setManagingOrganisation(AdtMessage source, EpisodeOfCare target) throws TransformException {
        target.setManagingOrganization(targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class));
    }

    private static void setPeriod(AdtMessage source, EpisodeOfCare target) throws ParseException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        Period period = DateTimeHelper.createPeriod(pv1Segment.getAdmitDateTime(), pv1Segment.getDischargeDateTime());

        if (period != null)
            target.setPeriod(period);
    }
}
