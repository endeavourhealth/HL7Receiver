package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.Encounter;

public abstract class EncounterClassVs {

    public static Encounter.EncounterClass convert(String patientClass) throws TransformException {

        patientClass = StringUtils.defaultString(patientClass).trim().toUpperCase();

        switch (patientClass) {
            case "EMERGENCY": return Encounter.EncounterClass.EMERGENCY;
            case "INPATIENT": return Encounter.EncounterClass.INPATIENT;
            case "OUTPATIENT": return Encounter.EncounterClass.OUTPATIENT;

            //Homerton Specific
            case "RECURRING": return Encounter.EncounterClass.OTHER;
            case "WAIT LIST": return Encounter.EncounterClass.OTHER;

            default: throw new TransformException(patientClass + " patient class not recognised");
        }
    }

    public static String convertOtherValues(String otherPatientClass) throws TransformException {

        otherPatientClass = StringUtils.defaultString(otherPatientClass).trim().toUpperCase();

        switch (otherPatientClass) {
            case "RECURRING": return "recurring";
            case "WAIT LIST": return "waitinglist";

            default: throw new TransformException(otherPatientClass + " other patient class not recognised");
        }
    }
}
