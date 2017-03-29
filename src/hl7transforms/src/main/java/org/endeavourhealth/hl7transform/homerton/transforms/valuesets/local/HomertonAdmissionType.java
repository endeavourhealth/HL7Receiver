package org.endeavourhealth.hl7transform.homerton.transforms.valuesets.local;

public enum HomertonAdmissionType {
    EMERGENCY_AE_OR_DENTAL("emergency-ae-or-dental", "Emergency - A&E/Dental"),
    EMERGENCY_OUTPAITENTS("emergency-outpatients", "Emergency - Outpatients clinic"),
    EMERGENCY_OTHER("emergency-other", "Emergency - Other"),
    MATERNITY_ANTE_PARTUM("maternity-ante-partum", "Maternity - Ante Partum"),
    MATERNITY_POST_PARTUM("maternity-post-partum", "Maternity - Post Partum"),
    BABY_BORN_IN_HOSPITAL("baby-born", "Baby Born in Hospital"),
    PLANNED("planned", "Planned"),
    BOOKED("booked", "Booked"),
    WAITING_LIST("wait-list", "Waiting List");

    private String code;
    private String description;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() { return "http://endeavourhealth.org/fhir/ValueSet/encounter-admission-type-homerton"; }

    HomertonAdmissionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
