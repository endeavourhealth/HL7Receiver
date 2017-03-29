package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7transform.homerton.transforms.valuesets.local.HomertonAdmissionType;

public abstract class HomertonAdmissionTypeVs {

    public static HomertonAdmissionType convert(String admissionType) {

        admissionType = StringUtils.defaultString(admissionType).trim().toLowerCase();

        switch (admissionType) {
            case "emergency-a\\t\\e/dental":
            case "emergency-a&e/dental": return HomertonAdmissionType.EMERGENCY_AE_OR_DENTAL;
            case "emergency-o/p clinic": return HomertonAdmissionType.EMERGENCY_OUTPAITENTS;
            case "emergency-other": return HomertonAdmissionType.EMERGENCY_OTHER;
            case "maternity-ante partum": return HomertonAdmissionType.MATERNITY_ANTE_PARTUM;
            case "maternity-post partum": return HomertonAdmissionType.MATERNITY_POST_PARTUM;
            case "baby born in hospital": return HomertonAdmissionType.BABY_BORN_IN_HOSPITAL;
            case "planned": return HomertonAdmissionType.PLANNED;
            case "waiting list": return HomertonAdmissionType.WAITING_LIST;
            case "booked": return HomertonAdmissionType.BOOKED;
            default: throw new NotImplementedException(admissionType + " admission type not recognised");
        }
    }
}
