package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.common.fhir.schema.HomertonAdmissionType;
import org.endeavourhealth.common.fhir.schema.HomertonDischargeDisposition;
import org.endeavourhealth.common.fhir.schema.HomertonEncounterType;
import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.segments.EvnSegment;
import org.endeavourhealth.hl7transform.common.converters.DateConverter;
import org.endeavourhealth.hl7transform.homerton.HomertonResourceContainer;
import org.endeavourhealth.hl7transform.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.homerton.transforms.valuesets.*;
import org.endeavourhealth.hl7transform.mapper.MapperException;
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

public class EncounterTransform extends HomertonTransformBase {

    public EncounterTransform(Mapper mapper, HomertonResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Encounter;
    }

    public void transform(AdtMessage source) throws ParseException, TransformException, MapperException {

        if (!source.hasPv1Segment())
            return;

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
        setPatient(target, targetResources.getPatient());
        setEpisodeOfCare(target, targetResources.getEpisodeOfCare());
        setServiceProvider(source, target);
        setLocations(source, target);
        setParticipants(source, target);

        // hospitalisation component
        setReason(source, target);
        setDischargeDisposition(source, target);
        setDischargeDestination(source, target);
        setAdmitSource(source.getPv1Segment(), target);

        targetResources.addResource(target);
    }

    protected void setId(AdtMessage source, Encounter target) throws TransformException, ParseException, MapperException {

        String patientIdentifierValue = PatientTransform.getPatientIdentifierValue(source, HomertonConstants.primaryPatientIdentifierTypeCode);
        String episodeIdentifierValue = EpisodeOfCareTransform.getEpisodeIdentifierValue(source, HomertonConstants.primaryEpisodeIdentifierAssigningAuthority);

        UUID encounterUuid = mapper.mapEncounterUuid(
                HomertonConstants.primaryPatientIdentifierTypeCode,
                patientIdentifierValue,
                HomertonConstants.primaryEpisodeIdentifierAssigningAuthority,
                episodeIdentifierValue,
                source.getEvnSegment().getRecordedDateTime());

        target.setId(encounterUuid.toString());
    }

    private static void setCurrentStatus(AdtMessage source, Encounter target) throws TransformException, ParseException {

        EvnSegment evnSegment = source.getEvnSegment();
        Pv1Segment pv1Segment = source.getPv1Segment();

        if (StringUtils.isNotBlank(pv1Segment.getAccountStatus()))
            target.setStatus(EncounterStateVs.convert(pv1Segment.getAccountStatus()));

        if (evnSegment.getRecordedDateTime() == null)
            throw new TransformException("Recorded date/time empty");

        target.setPeriod(createPeriod(evnSegment.getRecordedDateTime(), null));
    }

    private static Period createPeriod(Hl7DateTime start, Hl7DateTime end) throws ParseException {
        if ((start == null) && (end == null))
            return null;

        Period period = new Period();

        if (start != null)
            period.setStartElement((DateTimeType)DateConverter.getDateType(start));

        if (end != null)
            period.setEndElement((DateTimeType)DateConverter.getDateType(end));

        return period;
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

        Period admissionDate = createPeriod(pv1Segment.getAdmitDateTime(), null);
        addStatusHistoryComponent(admissionDate, Encounter.EncounterState.ARRIVED, target);

        Period dischargeDate = createPeriod(null, pv1Segment.getDischargeDateTime());
        addStatusHistoryComponent(dischargeDate, Encounter.EncounterState.FINISHED, target);

        if (pv2Segment != null) {

            Period expectedAdmitDate = createPeriod(pv2Segment.getExpectedAdmitDateTime(), null);
            addStatusHistoryComponent(expectedAdmitDate, Encounter.EncounterState.PLANNED, target);

            Period expectedDischargeDate = createPeriod(null, pv2Segment.getExpectedDischargeDateTime());
            addStatusHistoryComponent(expectedDischargeDate, Encounter.EncounterState.PLANNED, target);
        }
    }

    private static void setClass(AdtMessage source, Encounter target) throws TransformException {

        target.setClass_(EncounterClassVs.convert(source.getPv1Segment().getPatientClass()));

        if (target.getClass_() == Encounter.EncounterClass.OTHER) {

            Extension extension = new Extension()
                    .setUrl(FhirExtensionUri.ENCOUNTER_PATIENT_CLASS_OTHER)
                    .setValue(new CodeType(EncounterClassVs.convertOtherValues(source.getPv1Segment().getPatientClass())));

            target.getClass_Element().addExtension(extension);
        }
    }

    private static void setAdmissionType(AdtMessage source, Encounter target) {
        Pv1Segment pv1Segment = source.getPv1Segment();

        if (StringUtils.isNotEmpty(pv1Segment.getAdmissionType())) {

            HomertonAdmissionType admissionType = HomertonAdmissionTypeVs.convert(pv1Segment.getAdmissionType());

            target.addExtension(new Extension()
                    .setUrl(FhirExtensionUri.ENCOUNTER_ADMISSION_TYPE)
                    .setValue(new CodeableConcept()
                            .addCoding(new Coding()
                                    .setDisplay(admissionType.getDescription())
                                    .setCode(admissionType.getCode())
                                    .setSystem(admissionType.getSystem()))
                            .setText(pv1Segment.getAdmissionType())));
        }
    }

    private static void setType(AdtMessage source, Encounter target) throws TransformException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        if (StringUtils.isNotBlank(pv1Segment.getPatientType())) {

            HomertonEncounterType encounterType = HomertonEncounterTypeVs.convert(pv1Segment.getPatientType());

            CodeableConcept codeableConcept = new CodeableConcept()
                    .addCoding(new Coding()
                            .setSystem(encounterType.getSystem())
                            .setCode(encounterType.getCode())
                            .setDisplay(encounterType.getDescription()))
                    .setText(pv1Segment.getPatientType());

            target.addType(codeableConcept);
        }
    }

    private static void setPatient(Encounter target, Patient patient) {
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patient.getId()));
    }

    private static void setEpisodeOfCare(Encounter target, EpisodeOfCare episodeOfCare) {
        target.addEpisodeOfCare(ReferenceHelper.createReference(ResourceType.EpisodeOfCare, episodeOfCare.getId()));
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
        List<Reference> references = practitionerTransform.createHospitalPractitioners(xcns, targetResources.getHomertonOrganisationReference());

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

    private static void setDischargeDisposition(AdtMessage source, Encounter target) throws TransformException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        if (StringUtils.isEmpty(pv1Segment.getDischargeDisposition()))
            return;

        HomertonDischargeDisposition dischargeDisposition = HomertonDischargeDispositionVs.convert(pv1Segment.getDischargeDisposition());

        Encounter.EncounterHospitalizationComponent hospitalizationComponent = getHospitalisationComponent(target);

        hospitalizationComponent.setDischargeDisposition(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(dischargeDisposition.getSystem())
                        .setCode(dischargeDisposition.getCode())
                        .setDisplay(dischargeDisposition.getDescription()))
                .setText(pv1Segment.getDischargeDisposition()));
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
            case "nhs provider-general": return locationTransform.createClassOfLocation("NHS health care provider - general", null);
            case "nhs provider-mental health": return locationTransform.createClassOfLocation("NHS health care provider - mental health", null);
            case "usual place of residence": return locationTransform.createClassOfLocation("Usual place of residence", LocationPhysicalType.HO);
            case "temporary home": return locationTransform.createClassOfLocation("Temporary place of residence", LocationPhysicalType.HO);
            case "not applicable-died or stillbirth": return null;
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
