package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.schema.EncounterParticipantType;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Pl;
import org.endeavourhealth.transform.hl7v2.parser.segments.Pv1Segment;
import org.endeavourhealth.transform.hl7v2.parser.segments.Pv2Segment;
import org.endeavourhealth.transform.hl7v2.transform.converters.*;
import org.endeavourhealth.transform.hl7v2.transform.converters.ExtensionHelper;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Encounter;

import java.util.List;
import java.util.UUID;

public class PatientVisitTransform {

    public static Encounter fromHl7v2(Pv1Segment source, String sendingFacility) throws ParseException, TransformException {
        Encounter target = new Encounter();

        target.addIdentifier().setValue(IdentifierHelper.generateId(source.getVisitNumber()));

        target.setClass_(convertPatientClass(source.getPatientClass()));

        if (target.getClass_() == Encounter.EncounterClass.OTHER) {
            target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.ENCOUNTER_PATIENT_CLASS, source.getPatientClass()));
        }

        //Current Location
        if (source.getAssignedPatientLocation() != null) {
            Reference reference = getReference(source.getAssignedPatientLocation());

            if (reference != null) {
                target.addLocation()
                        .setStatus(Encounter.EncounterLocationStatus.ACTIVE)
                        .setLocation(reference);
            }
        }

        if (StringUtils.isNotBlank(source.getAdmissionType())){
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getAdmissionType()));
        }

        //Prior Location
        if (source.getPriorPatientLocation() != null) {
            Reference reference = getReference(source.getPriorPatientLocation());

            if (reference != null) {
                target.addLocation()
                        .setStatus(Encounter.EncounterLocationStatus.COMPLETED)
                        .setLocation(reference);
            }
        }

        if (source.getAttendingDoctor() != null) {
            for (Encounter.EncounterParticipantComponent epl : PractitionerTransform.createParticipantComponents(source.getAttendingDoctor()
                    , EncounterParticipantType.PRIMARY_PERFORMER.getDescription()))
                target.addParticipant(epl);
        }

        if (source.getReferringDoctor() != null) {
            for (Encounter.EncounterParticipantComponent epl : PractitionerTransform.createParticipantComponents(source.getReferringDoctor()
                    , EncounterParticipantType.REFERRER.getDescription()))
                target.addParticipant(epl);
        }

        if (source.getConsultingDoctor() != null) {
            for (Encounter.EncounterParticipantComponent epl : PractitionerTransform.createParticipantComponents(source.getConsultingDoctor()
                    , EncounterParticipantType.CONSULTANT.getDescription()))
                target.addParticipant(epl);
        }

        if (StringUtils.isNotBlank(source.getHospitalService()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getHospitalService()));

        //Temporary Location
        if (source.getTemporaryLocation() != null) {
            Reference reference = getReference(source.getTemporaryLocation());

            if (reference != null) {
                target.addLocation()
                        .setStatus(Encounter.EncounterLocationStatus.ACTIVE)
                        .setLocation(reference);
            }
        }

        if (StringUtils.isNotBlank(source.getAdmitSource())
                || StringUtils.isNotBlank(source.getDischargeDisposition())
                || StringUtils.isNotBlank(source.getDischargedToLocation())) {

            Encounter.EncounterHospitalizationComponent hospitalComponent = new Encounter.EncounterHospitalizationComponent();

            if (StringUtils.isNotBlank(source.getAdmitSource()))
                hospitalComponent.setAdmitSource(CodeableConceptHelper.getCodeableConceptFromString(source.getAdmitSource()));

            if (StringUtils.isNotBlank(source.getDischargeDisposition()))
                hospitalComponent.setDischargeDisposition(CodeableConceptHelper.getCodeableConceptFromString(source.getDischargeDisposition()));

            if (StringUtils.isNotBlank(source.getDischargedToLocation())) {
                String ln = source.getDischargedToLocation();
                Reference reference = new Reference();
                reference.setDisplay(ln).setReference(generateId(ln,ln));

                hospitalComponent.setDestination(reference);
            }

            target.setHospitalization(hospitalComponent);
        }

        if (StringUtils.isNotBlank(source.getAccountStatus())){
            target.setStatus(getState(source.getAccountStatus()));
        }

        if (source.getAdmitDateTime() != null || source.getDischargeDateTime() != null) {
            Period period = new Period();
            if (source.getAdmitDateTime() != null)
                period.setStart(DateHelper.fromLocalDateTime(source.getAdmitDateTime()));
            if (source.getDischargeDateTime() != null)
                period.setEnd(DateHelper.fromLocalDateTime(source.getDischargeDateTime()));
            target.setPeriod(period);
        }

        if (StringUtils.isNotBlank(source.getPatientType()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getPatientType()));


        if (source.getVisitNumber() != null) {
            Identifier identifier = IdentifierConverter.convert(source.getVisitNumber(), sendingFacility);

            if (identifier != null)
                target.addIdentifier(identifier);
        }

        if (source.getOtherHealthcareProvider() != null) {
            for (Encounter.EncounterParticipantComponent epl : PractitionerTransform.createParticipantComponents(source.getOtherHealthcareProvider()
                    , EncounterParticipantType.SECONDARY_PERFORMER.getDescription()))
                target.addParticipant(epl);
        }

        return target;
    }

    public static Encounter addAdditionalInformation(Encounter target, Pv2Segment source) throws TransformException, ParseException {

        if (source.getAccommodationCode() != null){
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getAccommodationCode().getAsString()));
        }

        if (source.getAdmitReason() != null) {
            target.addReason(CodeableConceptHelper.getCodeableConceptFromString(source.getAdmitReason().getAsString()));
        }

        if (source.getTransferReason() != null) {
            target.addReason(CodeableConceptHelper.getCodeableConceptFromString(source.getTransferReason().getAsString()));
        }

        if (source.getExpectedAdmitDateTime() != null || source.getExpectedDischargeDateTime() != null) {
            Encounter.EncounterStatusHistoryComponent shc = new Encounter.EncounterStatusHistoryComponent();

            shc.setStatus(Encounter.EncounterState.PLANNED);
            Period period = new Period();
            if (source.getExpectedAdmitDateTime() != null)
                period.setStart(DateHelper.fromLocalDateTime(source.getExpectedAdmitDateTime()));
            if (source.getExpectedDischargeDateTime() != null)
                period.setEnd(DateHelper.fromLocalDateTime(source.getExpectedDischargeDateTime()));
            shc.setPeriod(period);
            target.addStatusHistory(shc);
        }


        return target;
    }

    private static Encounter.EncounterState getState(String state) throws TransformException  {
        state = state.trim().toUpperCase();

        switch (state) {
            case "CANCELLED": return Encounter.EncounterState.CANCELLED;
            case "DISCHARGED": return Encounter.EncounterState.FINISHED;
            case "PENDING ARRIVAL": return Encounter.EncounterState.PLANNED;
            case "ACTIVE": return Encounter.EncounterState.INPROGRESS;
            case "PREADMIT": return Encounter.EncounterState.ARRIVED;

            default: throw new TransformException(state + " state not recognised");
        }
    }

    private static Reference getReference(Pl location) throws TransformException {
        List<Location> locations = LocationTransform.convert(location);

        if (locations.size() == 0)
            return null;

        Location finalLocation = locations.get(locations.size() - 1);

        return LocationTransform.createReferenceFromLocation(finalLocation);
    }

    private static Encounter.EncounterClass convertPatientClass(String patientClass) throws TransformException {
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

    private static String generateId(String uniqueString, String identifierString) {
        return UUID.nameUUIDFromBytes((identifierString + uniqueString).getBytes()).toString();
    }


}
