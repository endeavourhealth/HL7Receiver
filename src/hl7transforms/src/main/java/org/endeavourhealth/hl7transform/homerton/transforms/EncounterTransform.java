package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.hl7parser.segments.EvnSegment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.DateTimeHelper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7parser.segments.Pv2Segment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;

import java.util.List;
import java.util.UUID;

public class EncounterTransform extends ResourceTransformBase {

    public EncounterTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Encounter;
    }

    public Encounter transform(AdtMessage source) throws ParseException, TransformException, MapperException {

        if (!source.hasPv1Segment())
            return null;

        if (!source.hasEvnSegment())
            throw new TransformException("EVN segment not found");

        Encounter target = new Encounter();

        setId(source, target);

        // status, type, class, period
        setCurrentStatus(source, target);
        setStatusHistory(source, target);
        setClass(source, target);
        setType(source, target);
        setAdmissionType(source, target);

        // patient, episodeofcare, serviceprovider, locations, practitioners
        setPatient(target);
        setEpisodeOfCare(target);
        setServiceProvider(source, target);
        setLocations(source, target);
        setParticipants(source, target);

        // hospitalisation component
        setReason(source, target);
        setDischargeDisposition(source, target);
        setDischargeDestination(source, target);
        setAdmitSource(source.getPv1Segment(), target);

        return target;
    }

    protected void setId(AdtMessage source, Encounter target) throws TransformException, ParseException, MapperException {

        String patientIdentifierValue = PatientTransform.getPatientIdentifierValue(source, HomertonConstants.primaryPatientIdentifierTypeCode);
        String episodeIdentifierValue = EpisodeOfCareTransform.getEpisodeIdentifierValue(source, HomertonConstants.primaryEpisodeIdentifierAssigningAuthority);

        UUID encounterUuid = mapper.getResourceMapper().mapEncounterUuid(
                HomertonConstants.primaryPatientIdentifierTypeCode,
                patientIdentifierValue,
                HomertonConstants.primaryEpisodeIdentifierAssigningAuthority,
                episodeIdentifierValue,
                source.getEvnSegment().getRecordedDateTime());

        target.setId(encounterUuid.toString());
    }

    private void setCurrentStatus(AdtMessage source, Encounter target) throws TransformException, ParseException, MapperException {

        EvnSegment evnSegment = source.getEvnSegment();
        Pv1Segment pv1Segment = source.getPv1Segment();

        Encounter.EncounterState encounterState = this.mapper.getCodeMapper().mapAccountStatus(pv1Segment.getAccountStatus());

        if (encounterState != null)
            target.setStatus(encounterState);

        if (evnSegment.getRecordedDateTime() == null)
            throw new TransformException("Recorded date/time empty");

        target.setPeriod(DateTimeHelper.createPeriod(evnSegment.getRecordedDateTime(), null));
    }

    private static Encounter.EncounterStatusHistoryComponent createStatusHistoryComponent(Period period, Encounter.EncounterState encounterState) {
        return new Encounter.EncounterStatusHistoryComponent()
                .setPeriod(period)
                .setStatus(encounterState);
    }

    private static void addStatusHistoryComponent(Period period, Encounter.EncounterState encounterState, Encounter target) {
        if (period != null)
            target.addStatusHistory(createStatusHistoryComponent(period, encounterState));
    }

    private static void setStatusHistory(AdtMessage source, Encounter target) throws ParseException {

        Pv1Segment pv1Segment = source.getPv1Segment();
        Pv2Segment pv2Segment = source.getPv2Segment();

        Period admissionDate = DateTimeHelper.createPeriod(pv1Segment.getAdmitDateTime(), null);
        addStatusHistoryComponent(admissionDate, Encounter.EncounterState.ARRIVED, target);

        Period dischargeDate = DateTimeHelper.createPeriod(null, pv1Segment.getDischargeDateTime());
        addStatusHistoryComponent(dischargeDate, Encounter.EncounterState.FINISHED, target);

        if (pv2Segment != null) {

            Period expectedAdmitDate = DateTimeHelper.createPeriod(pv2Segment.getExpectedAdmitDateTime(), null);
            addStatusHistoryComponent(expectedAdmitDate, Encounter.EncounterState.PLANNED, target);

            Period expectedDischargeDate = DateTimeHelper.createPeriod(null, pv2Segment.getExpectedDischargeDateTime());
            addStatusHistoryComponent(expectedDischargeDate, Encounter.EncounterState.PLANNED, target);
        }
    }

    private void setClass(AdtMessage source, Encounter target) throws TransformException, MapperException {

        Encounter.EncounterClass encounterClass = this.mapper.getCodeMapper().mapPatientClass(source.getPv1Segment().getPatientClass());

        if (encounterClass != null) {
            target.setClass_(encounterClass);

            if (encounterClass == Encounter.EncounterClass.OTHER) {

                Extension extension = new Extension()
                        .setUrl(FhirExtensionUri.ENCOUNTER_PATIENT_CLASS_OTHER)
                        .setValue(new CodeType(convertOtherValues(source.getPv1Segment().getPatientClass())));

                target.getClass_Element().addExtension(extension);
            }
        }
    }

    // migrate to mapper
    private static String convertOtherValues(String otherPatientClass) throws TransformException {

        otherPatientClass = StringUtils.defaultString(otherPatientClass).trim().toUpperCase();

        switch (otherPatientClass) {
            case "RECURRING": return "recurring";
            case "WAIT LIST": return "waitinglist";

            default: throw new TransformException(otherPatientClass + " other patient class not recognised");
        }
    }

    private void setAdmissionType(AdtMessage source, Encounter target) throws MapperException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        CodeableConcept admissionType = this.mapper.getCodeMapper().mapAdmissionType(pv1Segment.getAdmissionType());

        if (admissionType != null) {
            target.addExtension(new Extension()
                    .setUrl(FhirExtensionUri.ENCOUNTER_ADMISSION_TYPE)
                    .setValue(admissionType));
        }
    }

    private void setType(AdtMessage source, Encounter target) throws TransformException, MapperException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        CodeableConcept patientType = this.mapper.getCodeMapper().mapPatientType(pv1Segment.getPatientType());

        if (patientType != null)
            target.addType(patientType);
    }

    private void setPatient(Encounter target) throws TransformException {
        target.setPatient(targetResources.getResourceReference(ResourceTag.PatientSubject, Patient.class));
    }

    private void setEpisodeOfCare(Encounter target) throws TransformException {
        EpisodeOfCare episodeOfCare = targetResources.getResourceSingle(EpisodeOfCare.class);

        target.addEpisodeOfCare(ReferenceHelper.createReferenceExternal(episodeOfCare));
    }

    private void setParticipants(AdtMessage source, Encounter target) throws TransformException, ParseException, MapperException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        addParticipantComponent(pv1Segment.getAttendingDoctor(), EncounterParticipantType.ATTENDER, target);
        addParticipantComponent(pv1Segment.getReferringDoctor(), EncounterParticipantType.REFERRER, target);
        addParticipantComponent(pv1Segment.getConsultingDoctor(), EncounterParticipantType.CONSULTANT, target);
        addParticipantComponent(pv1Segment.getAdmittingDoctor(), EncounterParticipantType.ADMITTER, target);
        addParticipantComponent(pv1Segment.getOtherHealthcareProvider(), EncounterParticipantType.SECONDARY_PERFORMER, target);
    }

    private void addParticipantComponent(
            List<Xcn> xcns,
            EncounterParticipantType type,
            Encounter target) throws TransformException, MapperException, ParseException {

        if (xcns == null)
            return;

        PractitionerTransform practitionerTransform = new PractitionerTransform(mapper, targetResources);
        List<Reference> references = practitionerTransform.createPractitioners(xcns);

        for (Reference reference : references) {
            target.addParticipant(new Encounter.EncounterParticipantComponent()
                    .addType(new CodeableConcept()
                            .addCoding(new Coding()
                                    .setCode(type.getCode())
                                    .setDisplay(type.getDescription())
                                    .setSystem(type.getSystem())))
                    .setIndividual(reference));
        }
    }

    private void setLocations(AdtMessage source, Encounter target) throws TransformException, MapperException, ParseException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        if (pv1Segment.getAssignedPatientLocation() != null)
            addLocation(pv1Segment.getAssignedPatientLocation(), Encounter.EncounterLocationStatus.ACTIVE, target);

        if (pv1Segment.getPriorPatientLocation() != null)
            addLocation(pv1Segment.getPriorPatientLocation(), Encounter.EncounterLocationStatus.COMPLETED, target);
    }

    private void addLocation(Pl location, Encounter.EncounterLocationStatus encounterLocationStatus, Encounter target) throws MapperException, TransformException, ParseException {

        Reference assignedLocationReference = new LocationTransform(mapper, targetResources)
                .createHomertonConstituentLocation(location);

        if (assignedLocationReference != null) {
            target.addLocation(new Encounter.EncounterLocationComponent()
                    .setLocation(assignedLocationReference)
                    .setStatus(encounterLocationStatus));
        }
    }

    private static void setReason(AdtMessage sourceMessage, Encounter target) throws TransformException {

        Pv2Segment pv2Segment = sourceMessage.getPv2Segment();

        if (pv2Segment.getAdmitReason() != null)
            target.addReason(new CodeableConcept().setText(pv2Segment.getAdmitReason().getAsString()));

        if (pv2Segment.getTransferReason() != null)
            target.addReason(new CodeableConcept().setText(pv2Segment.getTransferReason().getAsString()));
    }

    private void setDischargeDisposition(AdtMessage source, Encounter target) throws TransformException, MapperException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        CodeableConcept dischargeDisposition = this.mapper.getCodeMapper().mapDischargeDisposition(pv1Segment.getDischargeDisposition());

        if (dischargeDisposition != null) {
            Encounter.EncounterHospitalizationComponent hospitalizationComponent = getHospitalisationComponent(target);

            hospitalizationComponent.setDischargeDisposition(dischargeDisposition);
        }
    }

    private void setDischargeDestination(AdtMessage source, Encounter target) throws MapperException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        if (StringUtils.isEmpty(pv1Segment.getDischargedToLocation()))
            return;

        Reference dischargeClassOfLocation = getDischargeLocation(pv1Segment.getDischargedToLocation());

        if (dischargeClassOfLocation == null)
            return;

        Encounter.EncounterHospitalizationComponent hospitalizationComponent = getHospitalisationComponent(target);
        hospitalizationComponent.setDestination(dischargeClassOfLocation);
    }

    public Reference getDischargeLocation(String dischargeLocation) throws MapperException {

        dischargeLocation = StringUtils.defaultString(dischargeLocation).trim().toLowerCase();

        LocationTransform locationTransform = new LocationTransform(mapper, targetResources);

        switch (dischargeLocation) {
            case "usual place of residence": return locationTransform.createClassOfLocation("Usual place of residence", LocationPhysicalType.HO);
            case "temporary home": return locationTransform.createClassOfLocation("Temporary place of residence", LocationPhysicalType.HO);
            case "nhs provider-general": return locationTransform.createClassOfLocation("NHS health care provider - general", null);
            case "nhs provider-maternity": return locationTransform.createClassOfLocation("NHS health care provider - maternity", null);
            case "nhs provider-mental health": return locationTransform.createClassOfLocation("NHS health care provider - mental health", null);
            case "nhs nursing home": return locationTransform.createClassOfLocation("NHS nursing home", null);
            case "nhs medium secure": return locationTransform.createClassOfLocation("NHS medium secure unit", null);
            case "high security psychiatric (sco)": return locationTransform.createClassOfLocation("High security psychiatric unit (SCO)", null);
            case "non-nhs residential care": return locationTransform.createClassOfLocation("Non-NHS residential care provider", null);
            case "non-nhs hospital": return locationTransform.createClassOfLocation("Non-NHS hospital", null);
            case "non-nhs hospice": return locationTransform.createClassOfLocation("Non-NHS hospice", null);
            case "penal establishment/police station": return locationTransform.createClassOfLocation("Penal establishment/police station", null);
            case "not applicable-died or stillbirth": return null;
            case "repatriation from hsph": return null;
            case "not known": return null;

            default: throw new NotImplementedException(dischargeLocation + " not recognised");
        }
    }

    private static Encounter.EncounterHospitalizationComponent getHospitalisationComponent(Encounter target) {
        if (target.getHospitalization() == null)
            target.setHospitalization(new Encounter.EncounterHospitalizationComponent());

        return target.getHospitalization();
    }

    private static void setAdmitSource(Pv1Segment source, Encounter target) throws TransformException {
        if (StringUtils.isBlank(source.getAdmitSource()))
            return;

        Encounter.EncounterHospitalizationComponent hospitalizationComponent = getHospitalisationComponent(target);
        hospitalizationComponent.setAdmitSource(new CodeableConcept().setText(source.getAdmitSource()));
    }

    private void setServiceProvider(AdtMessage sourceMessage, Encounter target) throws TransformException, MapperException, ParseException {

        Pv1Segment pv1Segment = sourceMessage.getPv1Segment();

        OrganizationTransform organizationTransform = new OrganizationTransform(mapper, targetResources);
        Reference reference = organizationTransform.createHomertonHospitalServiceOrganisation(pv1Segment);

        if (reference != null)
            target.setServiceProvider(reference);
    }
}
