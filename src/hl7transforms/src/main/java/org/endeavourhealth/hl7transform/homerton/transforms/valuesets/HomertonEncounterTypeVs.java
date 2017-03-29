package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7transform.homerton.transforms.valuesets.local.HomertonEncounterType;

public abstract class HomertonEncounterTypeVs {

    public static HomertonEncounterType convert(String patientEncounterType) {
        patientEncounterType = StringUtils.defaultString(patientEncounterType).trim().toLowerCase();

        switch (patientEncounterType) {

            case "clinical measurement": return HomertonEncounterType.CLINICAL_MEASUREMENT;
            case "clinical measurement wait list": return HomertonEncounterType.CLINICAL_MEASUREMENT_WAIT_LIST;
            case "day case": return HomertonEncounterType.DAY_CASE;
            case "day case waiting list": return HomertonEncounterType.DAY_CASE_WAIT_LIST;
            case "emergency department": return HomertonEncounterType.EMERGENCY;
            case "inpatient": return HomertonEncounterType.INPATIENT;
            case "inpatient waiting list": return HomertonEncounterType.INPATIENT_WAIT_LIST;
            case "maternity": return HomertonEncounterType.MATERNITY;
            case "newborn": return HomertonEncounterType.NEWBORN;
            case "outpatient": return HomertonEncounterType.OUTPATIENT;
            case "outpatient referral": return HomertonEncounterType.OUTPAITENT_REFERRAL;
            case "radiology": return HomertonEncounterType.RADIOLOGY;
            case "radiology referral wait list": return HomertonEncounterType.RADIOLOGY_WAIT_LIST;
            case "regular day admission": return HomertonEncounterType.REGULAR_DAY_ADMISSION;

            default: throw new NotImplementedException(patientEncounterType + " homerton patient encounter type not recognised");
        }
    }
}
