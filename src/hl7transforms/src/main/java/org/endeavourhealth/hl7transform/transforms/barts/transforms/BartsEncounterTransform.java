package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.EvnSegment;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7parser.segments.Pv2Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.DateTimeHelper;
import org.endeavourhealth.hl7transform.common.transform.EpisodeOfCareCommon;
import org.endeavourhealth.hl7transform.common.transform.LocationCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class BartsEncounterTransform extends ResourceTransformBase {

    private static final Logger LOG = LoggerFactory.getLogger(BartsEncounterTransform.class);

    public BartsEncounterTransform(Mapper mapper, ResourceContainer targetResources) {
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

        return target;
    }

    protected void setId(AdtMessage source, Encounter target) throws TransformException, ParseException, MapperException {

        String patientIdentifierValue = BartsPatientTransform.getBartsPrimaryPatientIdentifierValue(source);
        String episodeIdentifierValue = BartsEpisodeOfCareTransform.getBartsPrimaryEpisodeIdentifierValue(source);

        UUID encounterUuid = mapper.getResourceMapper().mapEncounterUuid(
                null,
                BartsConstants.primaryPatientIdentifierAssigningAuthority,
                patientIdentifierValue,
                BartsConstants.primaryEpisodeIdentifierTypeCode,
                null,
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

            default: throw new TransformException(otherPatientClass + " other patient class not recognised");
        }
    }

    private void setType(AdtMessage source, Encounter target) throws TransformException, MapperException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        CodeableConcept patientType = this.mapper.getCodeMapper().mapPatientType(pv1Segment.getPatientType(), null);

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
    }

    private void addParticipantComponent(
            List<Xcn> xcns,
            EncounterParticipantType type,
            Encounter target) throws TransformException, MapperException, ParseException {

        if (xcns == null)
            return;

        BartsPractitionerTransform practitionerTransform = new BartsPractitionerTransform(mapper, targetResources);

        for (Xcn xcn : xcns) {
            Practitioner practitioner = practitionerTransform.createPractitioner(xcn);

            target.addParticipant(new Encounter.EncounterParticipantComponent()
                    .addType(new CodeableConcept()
                            .addCoding(new Coding()
                                    .setCode(type.getCode())
                                    .setDisplay(type.getDescription())
                                    .setSystem(type.getSystem())))
                    .setIndividual(ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId())));
        }
    }

    private void setLocations(AdtMessage source, Encounter target) throws TransformException, MapperException, ParseException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        if (pv1Segment.getAssignedPatientLocation() != null)
            addLocation(pv1Segment.getAssignedPatientLocation(), Encounter.EncounterLocationStatus.ACTIVE, target);
    }

    private void addLocation(Pl location, Encounter.EncounterLocationStatus encounterLocationStatus, Encounter target) throws MapperException, TransformException, ParseException {

        Reference assignedLocationReference = new BartsLocationTransform(mapper, targetResources)
                .createBartsConstituentLocation(location);

        if (assignedLocationReference != null) {
            target.addLocation(new Encounter.EncounterLocationComponent()
                    .setLocation(assignedLocationReference)
                    .setStatus(encounterLocationStatus));
        }
    }

    private static void setReason(AdtMessage sourceMessage, Encounter target) throws TransformException {

        Pv2Segment pv2Segment = sourceMessage.getPv2Segment();

        if (pv2Segment != null) {
            if (pv2Segment.getAdmitReason() != null)
                target.addReason(new CodeableConcept().setText(pv2Segment.getAdmitReason().getAsString()));

            if (pv2Segment.getTransferReason() != null)
                target.addReason(new CodeableConcept().setText(pv2Segment.getTransferReason().getAsString()));
        }
    }

    private void setDischargeDisposition(AdtMessage source, Encounter target) throws TransformException, MapperException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        CodeableConcept dischargeDisposition = this.mapper.getCodeMapper().mapDischargeDisposition(pv1Segment.getDischargeDisposition(), null);

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

        CodeableConcept codeableConcept = mapper.getCodeMapper().mapDischargeDestination(dischargeLocation, null);
        String locationTypeName = CodeableConceptHelper.getFirstDisplayTerm(codeableConcept);

        if (StringUtils.isEmpty(locationTypeName))
            return null;

        return LocationCommon.createClassOfLocation(locationTypeName, mapper);
    }

    private static Encounter.EncounterHospitalizationComponent getHospitalisationComponent(Encounter target) {
        if (target.getHospitalization() == null)
            target.setHospitalization(new Encounter.EncounterHospitalizationComponent());

        return target.getHospitalization();
    }

    private void setServiceProvider(AdtMessage sourceMessage, Encounter target) throws TransformException, MapperException, ParseException {

        Pv1Segment pv1Segment = sourceMessage.getPv1Segment();

        BartsOrganizationTransform organizationTransform = new BartsOrganizationTransform(mapper, targetResources);
        Reference reference = organizationTransform.createBartsHospitalServiceOrganisation(pv1Segment);

        if (reference != null)
            target.setServiceProvider(reference);
    }
}
