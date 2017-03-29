package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.endeavourhealth.hl7transform.common.TransformException;

public enum HomertonDischargeDisposition {
    ADMITTED_AS_INPATIENT("admitted-as-inpatient", "Admitted as inpatient"),
    DECEASED("deceased", "Deceased"),
    DISCHARGE_MENTAL_TRIBUNAL("discharge-mental-tribunal", "Discharge - mental tribunal"),
    DISCHARGE_NORMAL("normal-discharge", "Discharge - normal"),
    DISCHARGE_NORMAL_NO_FOLLOW_UP("no-follow-up-required", "Discharge - normal, no follow up required"),
    DISCHARGE_NORMAL_WITH_FOLLOW_UP("normal-discharge-with-follow-up", "Discharge - normal, with follow-up"),
    DISCHARGE_SELF_OR_RELATIVE("discharge-self-or-relative", "Discharge - self/relative"),
    LEFT_DEPT_BEFORE_TREATMENT("left-dept-before-treatment", "Left department before treatment"),
    OTHER("other", "Other"),
    REFERRAL_TO_GP("referral-to-gp", "Referred to general practitioner"),
    REFERRAL_TO_OUTPATIENT_CLINIC("referred-to-outpatient-clinic", "Referred to outpatient clinic"),
    REFERRAL_TO_AE_CLINIC("referred-to-ae-clinic", "Referred to A&E clinic"),
    REFERRAL_TO_FRACTURE_CLINIC("referred-to-fracture-clinic", "Referred to fracture clinic"),
    REFERRAL_TO_OTHER_HCP("referred-to-other-hcp", "Referred to other health care profession"),
    TRANSFER_TO_OTHER_HCP("transfer-to-other-hcp", "Transfer to other health care provider");

    private String code;
    private String description;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() { return "http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton"; }

    HomertonDischargeDisposition(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static HomertonDischargeDisposition fromCode(String code) throws TransformException {
        for (HomertonDischargeDisposition homertonDischargeDisposition : HomertonDischargeDisposition.values())
            if (homertonDischargeDisposition.getCode().equals(code))
                return homertonDischargeDisposition;

        throw new TransformException(code + " HomertonDischargeDisposition value not recognised");
    }
}