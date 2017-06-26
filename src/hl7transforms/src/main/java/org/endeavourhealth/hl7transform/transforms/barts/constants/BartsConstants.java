package org.endeavourhealth.hl7transform.transforms.barts.constants;

public class BartsConstants {

    public static final String odsCode = "R1H";

    public static final String sendingFacility = "2.16.840.1.113883.3.2540";                                // MSH.4

    public static final String primaryPatientIdentifierAssigningAuthority = "2.16.840.1.113883.3.2540.1";   // PID.3
    public static final String primaryEpisodeIdentifierTypeCode = "VISITID";                                // PV1.19
    public static final String practitionerOrgDoctorNumberAssigningAuth = "DRNBR";                          // XCN.9
    public static final String practitionerPersonnelIdAssigningAuth = "NHSPRSNLID";                         // XCN.9
}
