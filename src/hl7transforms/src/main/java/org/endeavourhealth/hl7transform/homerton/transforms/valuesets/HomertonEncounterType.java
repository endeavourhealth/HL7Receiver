package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.endeavourhealth.hl7transform.common.TransformException;

public enum HomertonEncounterType {

    CLINICAL_MEASUREMENT("clinical-measurement", "Clinical measurement"),
    CLINICAL_MEASUREMENT_WAIT_LIST("clinical-measurement-wait-list", "Clinical measurement waiting list"),
    DAY_CASE("day-case", "Day case"),
    DAY_CASE_WAIT_LIST("day-case-wait-list", "Day case waiting list"),
    EMERGENCY("emergency", "Emergency"),
    INPATIENT("inpatient", "Inpatient"),
    INPATIENT_WAIT_LIST("inpatient-wait-list", "Inpatient waiting list"),
    MATERNITY("maternity", "Maternity waiting list"),
    NEWBORN("newborn", "Newborn"),
    OUTPATIENT("outpatient", "Outpatient"),
    OUTPAITENT_REFERRAL("outpatient-referral", "Outpatient referral"),
    RADIOLOGY("radiology", "Radiology"),
    RADIOLOGY_WAIT_LIST("radiology-wait-list", "Radiology wait list"),
    REGULAR_DAY_ADMISSION("regular-day-admission", "Regular day admission");

    private String code;
    private String description;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return "http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton";
    }

    HomertonEncounterType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static HomertonEncounterType fromCode(String code) throws TransformException {
        for (HomertonEncounterType homertonEncounterType : HomertonEncounterType.values())
            if (homertonEncounterType.getCode().equals(code))
                return homertonEncounterType;

        throw new TransformException(code + " HomertonEncounterType value not recognised");
    }
}