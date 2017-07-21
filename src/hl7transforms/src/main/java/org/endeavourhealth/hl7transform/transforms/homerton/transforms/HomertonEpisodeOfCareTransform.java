package org.endeavourhealth.hl7transform.transforms.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.transform.EpisodeOfCareCommon;
import org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.common.converters.DateTimeHelper;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class HomertonEpisodeOfCareTransform extends ResourceTransformBase {

    private static final Logger LOG = LoggerFactory.getLogger(HomertonEpisodeOfCareTransform.class);

    public HomertonEpisodeOfCareTransform(Mapper mapper, ResourceContainer targetResources) {
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

        setPatient(target);

        setManagingOrganisation(source, target);

        setStatusAndPeriod(source, target);

        return target;
    }

    protected void setId(AdtMessage source, EpisodeOfCare target) throws TransformException, MapperException {
        UUID episodeUuid = getHomertonMappedEpisodeOfCareUuid(source, mapper);
        target.setId(episodeUuid.toString());
    }

    public static String getHomertonPrimaryEpisodeIdentifierValue(AdtMessage source) {
        return EpisodeOfCareCommon.getEpisodeIdentifierValueByAssigningAuthority(source, HomertonConstants.primaryEpisodeIdentifierAssigningAuthority);
    }

    public static String getHomertonPrimaryEpisodeIdentifierValue(List<Cx> cxs) {
        return EpisodeOfCareCommon.getEpisodeIdentifierValueByAssigningAuthority(cxs, HomertonConstants.primaryEpisodeIdentifierAssigningAuthority);
    }

    private static UUID getHomertonMappedEpisodeOfCareUuid(AdtMessage source, Mapper mapper) throws MapperException {
        String patientIdentifierValue = HomertonPatientTransform.getHomertonPrimaryPatientIdentifierValue(source);
        String episodeIdentifierValue = getHomertonPrimaryEpisodeIdentifierValue(source);

        return getHomertonMappedEpisodeOfCareUuid(patientIdentifierValue, episodeIdentifierValue, mapper);
    }

    public static UUID getHomertonMappedEpisodeOfCareUuid(List<Cx> patientIdentifierList, List<Cx> episodeIdentifierList, Mapper mapper) throws MapperException, ParseException {
        String patientIdentifierValue = HomertonPatientTransform.getHomertonPrimaryPatientIdentifierValue(patientIdentifierList);
        String episodeIdentifierValue = getHomertonPrimaryEpisodeIdentifierValue(episodeIdentifierList);

        return getHomertonMappedEpisodeOfCareUuid(patientIdentifierValue, episodeIdentifierValue, mapper);
    }

    private static UUID getHomertonMappedEpisodeOfCareUuid(String patientIdentifierValue, String episodeIdentifierValue, Mapper mapper) throws MapperException {
        return mapper.getResourceMapper().mapEpisodeUuid(
                HomertonConstants.primaryPatientIdentifierTypeCode,
                null,
                patientIdentifierValue,
                null,
                HomertonConstants.primaryEpisodeIdentifierAssigningAuthority,
                episodeIdentifierValue);
    }


    private void setIdentifiers(AdtMessage source, EpisodeOfCare target) throws TransformException, MapperException {

        Identifier visitNumber = IdentifierConverter.createIdentifier(source.getPv1Segment().getVisitNumber(), getResourceType(), mapper);

        if (visitNumber != null)
            target.addIdentifier(visitNumber);

        Identifier alternateVisitId = IdentifierConverter.createIdentifier(source.getPv1Segment().getAlternateVisitID(), getResourceType(), mapper);

        if (alternateVisitId != null)
            target.addIdentifier(alternateVisitId);
    }

    private void setPatient(EpisodeOfCare target) throws TransformException {
        target.setPatient(targetResources.getResourceReference(ResourceTag.PatientSubject, Patient.class));
    }

    private void setManagingOrganisation(AdtMessage source, EpisodeOfCare target) throws TransformException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        String servicingFacilityName = StringUtils.trim(pv1Segment.getServicingFacility()).toUpperCase();

        if (StringUtils.isNotBlank(servicingFacilityName))
            if (!servicingFacilityName.equals(HomertonConstants.servicingFacility))
                throw new TransformException("Hospital servicing facility of " + servicingFacilityName + " not recognised");

        target.setManagingOrganization(targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class));
    }

    private void setStatusAndPeriod(AdtMessage source, EpisodeOfCare target) throws TransformException, ParseException, MapperException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        String accountStatus = pv1Segment.getAccountStatus();
        Hl7DateTime admitDate = pv1Segment.getAdmitDateTime();
        Hl7DateTime dischargeDate = pv1Segment.getDischargeDateTime();
        Hl7DateTime eventRecordedDate = source.getEvnSegment().getRecordedDateTime();

        EpisodeOfCareCommon.setStatusAndPeriod(target, accountStatus, admitDate, dischargeDate, eventRecordedDate, mapper);
    }
}
