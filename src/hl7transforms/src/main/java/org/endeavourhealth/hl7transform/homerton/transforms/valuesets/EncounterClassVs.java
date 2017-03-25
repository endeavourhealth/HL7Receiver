package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.Encounter;

public abstract class EncounterClassVs {

    public static Encounter.EncounterClass convert(String patientClass) throws TransformException {
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
}
