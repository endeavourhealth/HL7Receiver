package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.hl7transform.homerton.parser.zsegments.HomertonSegmentName;
import org.endeavourhealth.hl7transform.homerton.parser.zsegments.ZviSegment;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7parser.segments.Pv2Segment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.CodeableConceptHelper;
import org.endeavourhealth.hl7transform.common.converters.ExtensionHelper;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EncounterTransform {
    private static final String encounterAssigningAuthority = "Homerton FIN";

    private Mapper mapper;
    private ResourceContainer targetResources;

    public EncounterTransform(Mapper mapper, ResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public void transform(AdtMessage sourceMessage) throws ParseException, TransformException, MapperException {

        if (!sourceMessage.hasPv1Segment())
            return;

        Encounter target = new Encounter();

        setId(sourceMessage, target);
        setIdentifiers(sourceMessage, target);
        setStatus(sourceMessage, target);
        setStatusHistory(sourceMessage, target);
        setClass(sourceMessage, target);
        setType(sourceMessage, target);
        setPatient(target, targetResources.getPatient());
        setEpisodeOfCare(target, targetResources.getEpisodeOfCare());
        setParticipants(sourceMessage, target);
        setPeriod(sourceMessage, target);
        setReason(sourceMessage, target);
        setHospitalisationElement(sourceMessage.getPv1Segment(), target);
        setLocations(sourceMessage, target);

        // set service provider

        // determine what to do with ACC segment information

        targetResources.addResource(target);
    }

    private void setId(AdtMessage sourceMessage, Encounter target) throws MapperException, TransformException {
        String uniqueIdentifyingString = getUniqueEncounterString(sourceMessage);
        UUID resourceUuid = mapper.mapResourceUuid(ResourceType.Encounter, uniqueIdentifyingString);

        target.setId(resourceUuid.toString());
    }

    public static String getUniqueEncounterString(AdtMessage message) throws TransformException {
        Cx cx = message.getPidSegment().getPatientAccountNumber();

        if (cx == null)
            throw new TransformException("Encounter number (PID.18) is null");

        if (StringUtils.isEmpty(cx.getAssigningAuthority()))
            throw new TransformException("Encounter number (PID.18) assigning authority is empty");

        if (StringUtils.isEmpty(cx.getId()))
            throw new TransformException("Encounter number (PID.18) id is empty");

        if (!encounterAssigningAuthority.equals(cx.getAssigningAuthority()))
            throw new TransformException("Encounter number assigning authority does not match");

        return StringUtils.deleteWhitespace(PatientTransform.getUniquePatientString(message) + "-Encounter-" + cx.getAssigningAuthority() + "-" + cx.getId());
    }

    private static void setIdentifiers(AdtMessage source, Encounter target) {

        List<Cx> cxs = new ArrayList<>();
        cxs.add(source.getPv1Segment().getVisitNumber());
        cxs.add(source.getPv1Segment().getAlternateVisitID());

        for (Cx cx : cxs) {
            if (cx != null) {
                if (!StringUtils.isBlank(cx.getId())) {

                    String identifierSystem = FhirUri.getHl7v2LocalEncounterIdentifierSystem(
                            source.getMshSegment().getSendingFacility(),
                            cx.getAssigningAuthority(),
                            cx.getIdentifierTypeCode());

                    target.addIdentifier(new Identifier()
                            .setSystem(identifierSystem)
                            .setValue(cx.getId()));
                }
            }
        }
    }

    private static void setStatus(AdtMessage source, Encounter target) throws TransformException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        if (StringUtils.isNotBlank(pv1Segment.getAccountStatus()))
            target.setStatus(getEncounterStatus(pv1Segment.getAccountStatus()));
    }

    private static void setStatusHistory(AdtMessage source, Encounter target) throws ParseException {

        Pv2Segment pv2Segment = source.getPv2Segment();
        ZviSegment zviSegment = source.getSegment(HomertonSegmentName.ZVI, ZviSegment.class);

        if (pv2Segment != null) {

            if (pv2Segment.getExpectedAdmitDateTime() != null || pv2Segment.getExpectedDischargeDateTime() != null) {

                Period period = new Period();

                if (pv2Segment.getExpectedAdmitDateTime() != null)
                    period.setStart(pv2Segment.getExpectedAdmitDateTime().asDate());

                if (pv2Segment.getExpectedDischargeDateTime() != null)
                    period.setEnd(pv2Segment.getExpectedDischargeDateTime().asDate());

                target.addStatusHistory(new Encounter.EncounterStatusHistoryComponent()
                        .setStatus(Encounter.EncounterState.PLANNED)
                        .setPeriod(period));
            }
        }

        if (zviSegment != null) {
            if (zviSegment.getAssignToLocationDate() != null) {
                target.addStatusHistory(
                        new Encounter.EncounterStatusHistoryComponent()
                                .setStatus(Encounter.EncounterState.ARRIVED)
                                .setPeriod(new Period()
                                        .setStart(zviSegment.getAssignToLocationDate().asDate())));
            }
        }
    }

    private static Encounter.EncounterState getEncounterStatus(String state) throws TransformException  {
        state = state.trim().toUpperCase();

        switch (state) {
            case "CANCELLED": return Encounter.EncounterState.CANCELLED;
            case "DISCHARGED": return Encounter.EncounterState.FINISHED;
            case "PENDING ARRIVAL": return Encounter.EncounterState.PLANNED;
            case "ACTIVE": return Encounter.EncounterState.INPROGRESS;
            case "PREADMIT": return Encounter.EncounterState.ARRIVED;
            case "CANCELLED PENDING ARRIVAL": return Encounter.EncounterState.CANCELLED;

            default: throw new TransformException(state + " state not recognised");
        }
    }

    private static void setClass(AdtMessage source, Encounter target) throws TransformException {
        target.setClass_(convertEncounterClass(source.getPv1Segment().getPatientClass()));

        if (target.getClass_() == Encounter.EncounterClass.OTHER)
            target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.ENCOUNTER_PATIENT_CLASS, source.getPv1Segment().getPatientClass()));
    }

    private static Encounter.EncounterClass convertEncounterClass(String patientClass) throws TransformException {
        patientClass = patientClass.trim().toUpperCase();

        switch (patientClass) {
            case "OUTPATIENT": return Encounter.EncounterClass.OUTPATIENT;
            case "EMERGENCY": return Encounter.EncounterClass.EMERGENCY;
            case "INPATIENT": return Encounter.EncounterClass.INPATIENT;

            //Homerton Specific
            case "RECURRING": return Encounter.EncounterClass.OTHER;
            case "WAIT LIST": return Encounter.EncounterClass.OTHER;
            default: throw new TransformException(patientClass + " patient class not recognised");
        }
    }

    private static void setType(AdtMessage source, Encounter target) throws TransformException {

        Pv1Segment pv1Segment = source.getPv1Segment();
        Pv2Segment pv2Segment = source.getPv2Segment();
        ZviSegment zviSegment = source.getSegment(HomertonSegmentName.ZVI, ZviSegment.class);

        if (StringUtils.isNotBlank(pv1Segment.getAdmissionType()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(pv1Segment.getAdmissionType()));

        if (StringUtils.isNotBlank(pv1Segment.getHospitalService()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(pv1Segment.getHospitalService()));

        if (StringUtils.isNotBlank(pv1Segment.getPatientType()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(pv1Segment.getPatientType()));

        if (pv2Segment != null)
            if (pv2Segment.getAccommodationCode() != null)
                target.addType(CodeableConceptHelper.getCodeableConceptFromString(pv2Segment.getAccommodationCode().getAsString()));

        if (zviSegment != null)
            if (StringUtils.isNotBlank(zviSegment.getServiceCategory()))
                target.addType(CodeableConceptHelper.getCodeableConceptFromString(zviSegment.getServiceCategory()));

        if (zviSegment != null)
            if (StringUtils.isNotBlank(zviSegment.getAdmitMode()))
                target.addType(CodeableConceptHelper.getCodeableConceptFromString(zviSegment.getAdmitMode()));
    }

    private static void setPatient(Encounter target, Patient patient) {
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patient.getId()));
    }

    private static void setEpisodeOfCare(Encounter target, EpisodeOfCare episodeOfCare) {
        target.addEpisodeOfCare(ReferenceHelper.createReference(ResourceType.EpisodeOfCare, episodeOfCare.getId()));
    }

    private void setParticipants(AdtMessage source, Encounter target) throws TransformException, ParseException, MapperException {

        Pv1Segment pv1Segment = source.getPv1Segment();
        String sendingFacility = source.getMshSegment().getSendingFacility();

        addParticipantComponent(pv1Segment.getAttendingDoctor(), EncounterParticipantType.ATTENDER, target, sendingFacility);
        addParticipantComponent(pv1Segment.getReferringDoctor(), EncounterParticipantType.REFERRER, target, sendingFacility);
        addParticipantComponent(pv1Segment.getConsultingDoctor(), EncounterParticipantType.CONSULTANT, target, sendingFacility);
        addParticipantComponent(pv1Segment.getAdmittingDoctor(), EncounterParticipantType.ADMITTER, target, sendingFacility);
        addParticipantComponent(pv1Segment.getOtherHealthcareProvider(), EncounterParticipantType.SECONDARY_PERFORMER, target, sendingFacility);
    }

    private void addParticipantComponent(
            List<Xcn> xcns,
            EncounterParticipantType type,
            Encounter target,
            String sendingFacility) throws TransformException, MapperException {

        if (xcns == null)
            return;

        PractitionerTransform practitionerTransform = new PractitionerTransform(sendingFacility, mapper, targetResources);
        List<Reference> references = practitionerTransform.transformAndGetReferences(xcns);

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

    private void setLocations(AdtMessage source, Encounter target) throws TransformException, MapperException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        addLocation(pv1Segment.getAssignedPatientLocation(), Encounter.EncounterLocationStatus.ACTIVE, target);
        addLocation(pv1Segment.getPriorPatientLocation(), Encounter.EncounterLocationStatus.COMPLETED, target);
    }

    private void addLocation(Pl location, Encounter.EncounterLocationStatus encounterLocationStatus, Encounter target) throws MapperException {

        if (location != null) {
            Reference assignedLocationReference = new LocationTransform(mapper, targetResources)
                    .transformAndGetReference(location);

            if (assignedLocationReference != null) {
                target.addLocation(new Encounter.EncounterLocationComponent()
                        .setLocation(assignedLocationReference)
                        .setStatus(encounterLocationStatus));
            }
        }
    }

    private static void setPeriod(AdtMessage sourceMessage, Encounter target) throws ParseException {

        Pv1Segment pv1Segment = sourceMessage.getPv1Segment();

        if ((pv1Segment.getAdmitDateTime() != null) && (pv1Segment.getDischargeDateTime() != null)) {

            Period period = new Period();

            if (pv1Segment.getAdmitDateTime() != null)
                period.setStart(pv1Segment.getAdmitDateTime().asDate());

            if (pv1Segment.getDischargeDateTime() != null)
                period.setEnd(pv1Segment.getDischargeDateTime().asDate());

            target.setPeriod(period);
        }
    }

    private static void setReason(AdtMessage sourceMessage, Encounter target) throws TransformException {

        Pv2Segment pv2Segment = sourceMessage.getPv2Segment();

        if (pv2Segment.getAdmitReason() != null)
            target.addReason(CodeableConceptHelper.getCodeableConceptFromString(pv2Segment.getAdmitReason().getAsString()));

        if (pv2Segment.getTransferReason() != null)
            target.addReason(CodeableConceptHelper.getCodeableConceptFromString(pv2Segment.getTransferReason().getAsString()));
    }

    private static void setHospitalisationElement(Pv1Segment source, Encounter target) throws TransformException {
        if (StringUtils.isNotBlank(source.getAdmitSource())
                || StringUtils.isNotBlank(source.getDischargeDisposition())
                || StringUtils.isNotBlank(source.getDischargedToLocation())) {

            Encounter.EncounterHospitalizationComponent hospitalComponent = new Encounter.EncounterHospitalizationComponent();

            if (StringUtils.isNotBlank(source.getAdmitSource()))
                hospitalComponent.setAdmitSource(CodeableConceptHelper.getCodeableConceptFromString(source.getAdmitSource()));

            if (StringUtils.isNotBlank(source.getDischargeDisposition()))
                hospitalComponent.setDischargeDisposition(CodeableConceptHelper.getCodeableConceptFromString(source.getDischargeDisposition()));

            if (StringUtils.isNotBlank(source.getDischargedToLocation()))
                hospitalComponent.setDestination(new Reference().setDisplay(source.getDischargedToLocation()));

            target.setHospitalization(hospitalComponent);
        }
    }
}
