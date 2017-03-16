package org.endeavourhealth.transform.hl7v2.transform.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.transform.hl7v2.mapper.Mapper;
import org.endeavourhealth.transform.hl7v2.mapper.MapperException;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7parser.segments.Pv2Segment;
import org.endeavourhealth.transform.hl7v2.profiles.TransformProfile;
import org.endeavourhealth.transform.hl7v2.transform.ResourceContainer;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.endeavourhealth.transform.hl7v2.transform.converters.*;
import org.endeavourhealth.transform.hl7v2.transform.converters.ExtensionHelper;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EncounterTransform {
    public static void fromHl7v2(AdtMessage sourceMessage, TransformProfile transformProfile, Mapper mapper, ResourceContainer targetResources) throws ParseException, TransformException, MapperException {
        Validate.notNull(sourceMessage);
        Validate.notNull(targetResources);
        Validate.notNull(transformProfile);
        Validate.notNull(mapper);

        if (sourceMessage.getPv1Segment() == null)
            return;

        Encounter target = new Encounter();

        setId(sourceMessage, target, transformProfile, mapper);
        setIdentifiers(sourceMessage, target);
        setStatus(sourceMessage, target);
        setStatusHistory(sourceMessage, target);
        setClass(sourceMessage, target);
        setType(sourceMessage, target);
        setPatient(target, targetResources);

        // set episode of care

        setParticipants(sourceMessage, target, mapper, targetResources);
        setPeriod(sourceMessage, target);
        setReason(sourceMessage, target);
        setHospitalisationElement(sourceMessage.getPv1Segment(), target);
        setLocations(sourceMessage, target, mapper, targetResources);

        // set service provider

        // determine what to do with ACC segment information

        // site specific
        transformProfile.postTransformEncounter(sourceMessage, target);

        targetResources.add(target);
    }

    private static void setId(AdtMessage sourceMessage, Encounter target, TransformProfile transformProfile, Mapper mapper) throws MapperException, TransformException {
        String uniqueIdentifyingString = transformProfile.getUniqueEncounterString(sourceMessage);
        UUID resourceUuid = mapper.mapResourceUuid(ResourceType.Encounter, uniqueIdentifyingString);

        target.setId(resourceUuid.toString());
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

        if (StringUtils.isNotBlank(pv1Segment.getAdmissionType()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(pv1Segment.getAdmissionType()));

        if (StringUtils.isNotBlank(pv1Segment.getHospitalService()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(pv1Segment.getHospitalService()));

        if (StringUtils.isNotBlank(pv1Segment.getPatientType()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(pv1Segment.getPatientType()));

        if (pv2Segment != null)
            if (pv2Segment.getAccommodationCode() != null)
                target.addType(CodeableConceptHelper.getCodeableConceptFromString(pv2Segment.getAccommodationCode().getAsString()));
    }

    private static void setPatient(Encounter target, ResourceContainer targetResources) {
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, targetResources.getPatient().getId()));
    }

    private static void setParticipants(AdtMessage source, Encounter target, Mapper mapper, ResourceContainer targetResources) throws TransformException, ParseException, MapperException {

        Pv1Segment pv1Segment = source.getPv1Segment();
        String sendingFacility = source.getMshSegment().getSendingFacility();

        addParticipantComponent(pv1Segment.getAttendingDoctor(), EncounterParticipantType.ATTENDER, target, mapper, targetResources, sendingFacility);
        addParticipantComponent(pv1Segment.getReferringDoctor(), EncounterParticipantType.REFERRER, target, mapper, targetResources, sendingFacility);
        addParticipantComponent(pv1Segment.getConsultingDoctor(), EncounterParticipantType.CONSULTANT, target, mapper, targetResources, sendingFacility);
        addParticipantComponent(pv1Segment.getAdmittingDoctor(), EncounterParticipantType.ADMITTER, target, mapper, targetResources, sendingFacility);
        addParticipantComponent(pv1Segment.getOtherHealthcareProvider(), EncounterParticipantType.SECONDARY_PERFORMER, target, mapper, targetResources, sendingFacility);
    }

    private static void addParticipantComponent(
            List<Xcn> xcns,
            EncounterParticipantType type,
            Encounter target,
            Mapper mapper,
            ResourceContainer targetResources,
            String sendingFacility) throws TransformException, MapperException {

        if (xcns == null)
            return;

        List<Reference> references = PractitionerTransform.transformAndGetReferences(xcns, sendingFacility, mapper, targetResources);

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

    private static void setLocations(AdtMessage source, Encounter target, Mapper mapper, ResourceContainer targetResources) throws TransformException, MapperException {

        Pv1Segment pv1Segment = source.getPv1Segment();

        addLocation(pv1Segment.getAssignedPatientLocation(), Encounter.EncounterLocationStatus.ACTIVE, target, mapper, targetResources);
        addLocation(pv1Segment.getPriorPatientLocation(), Encounter.EncounterLocationStatus.COMPLETED, target, mapper, targetResources);
    }

    private static void addLocation(
            Pl location,
            Encounter.EncounterLocationStatus encounterLocationStatus,
            Encounter target,
            Mapper mapper,
            ResourceContainer targetResources) throws MapperException {

        if (location != null) {
            Reference assignedLocationReference = LocationTransform.transformAndGetReference(location, mapper, targetResources);

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
