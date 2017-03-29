package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7transform.homerton.transforms.valuesets.local.HomertonDischargeDisposition;

public abstract class HomertonDischargeDispositionVs {

    public static HomertonDischargeDisposition convert(String dischargeDisposition) {

        dischargeDisposition = StringUtils.defaultString(dischargeDisposition).trim().toLowerCase();

        switch (dischargeDisposition) {
            case "admitted as inpatient": return HomertonDischargeDisposition.ADMITTED_AS_INPATIENT;
            case "deceased": return HomertonDischargeDisposition.DECEASED;
            case "discharge-mental tribunal": return HomertonDischargeDisposition.DISCHARGE_MENTAL_TRIBUNAL;
            case "discharge-self/relative": return HomertonDischargeDisposition.DISCHARGE_SELF_OR_RELATIVE;
            case "left department before treatment": return HomertonDischargeDisposition.LEFT_DEPT_BEFORE_TREATMENT;
            case "no follow up required": return HomertonDischargeDisposition.DISCHARGE_NORMAL_NO_FOLLOW_UP;
            case "normal discharge": return HomertonDischargeDisposition.DISCHARGE_NORMAL;
            case "normal discharge with follow up":
            case "regular discharge with follow-up": return HomertonDischargeDisposition.DISCHARGE_NORMAL_WITH_FOLLOW_UP;
            case "other": return HomertonDischargeDisposition.OTHER;
            case "referral to general practitioner": return HomertonDischargeDisposition.REFERRAL_TO_GP;
            case "referral to outpatient clinic": return HomertonDischargeDisposition.REFERRAL_TO_OUTPATIENT_CLINIC;
            case "referred to a\\t\\e clinic":
            case "referred to a&e clinic": return HomertonDischargeDisposition.REFERRAL_TO_AE_CLINIC;
            case "referred to fracture clinic": return HomertonDischargeDisposition.REFERRAL_TO_FRACTURE_CLINIC;
            case "referred to other health care profession": return HomertonDischargeDisposition.REFERRAL_TO_OTHER_HCP;
            case "transferred to other health care provide": return HomertonDischargeDisposition.TRANSFER_TO_OTHER_HCP;

            default: throw new NotImplementedException(dischargeDisposition + " discharge disposition not recognised");
        }
    }

/*
        Discharge locations

        "NHS Provider-General"
        "NHS Provider-Mental Health"
        "Not Applicable-Died or Stillbirth"
        "Not Known"
        "Temporary Home"
        "Usual Place of Residence"
*/
}
