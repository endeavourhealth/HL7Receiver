package org.endeavourhealth.hl7transform.homerton.transforms;

public class HomertonConstants {
    public static final String odsCode = "RQX";
    public static final String organisationName = "Homerton University Hospital NHS Foundation Trust";
    public static final String addressLine = "Homerton Row";
    public static final String addressCity = "London";
    public static final String addressPostcode = "E9 6SR";

    public static final String odsSiteCode = "RQXM1";
    public static final String locationName = "Homerton University Hospital";

    public static final String servicingFacility = "HOMERTON UNIVER";  // PV1.39
    public static final String locationFacility = servicingFacility;   // PV1.3.4
    public static final String locationBuilding = "HOMERTON UH";       // PV1.3.7
    public static final String primaryPatientIdentifierTypeCode = "CNN";  // PID.3
    public static final String primaryEpisodeIdentifierAssigningAuthority = "Homerton FIN";  // PID.19
    public static final String primaryPractitionerIdentifierTypeCode = "Personnel Primary Identifier";
}
