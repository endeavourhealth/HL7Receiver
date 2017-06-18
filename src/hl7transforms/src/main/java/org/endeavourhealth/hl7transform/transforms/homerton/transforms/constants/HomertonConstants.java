package org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants;

public class HomertonConstants {

    public static final String odsCode = "RQX";

    public static final String sendingFacility = "HOMERTON";  //MSH.4
    public static final String servicingFacility = "HOMERTON UNIVER";  // PV1.39
    public static final String locationFacility = servicingFacility;   // PV1.3.4

    public static final String odsSiteCodeHomerton = "RQXM1";
    public static final String odsSiteCodeStLeonards = "5C451";

    public static final String locationBuildingHomerton = "HOMERTON UH";       // PV1.3.7
    public static final String locationBuildingStLeonards = "St Leonard's Hospital";    // PV1.3.7

    public static final String primaryPatientIdentifierTypeCode = "CNN";  // PID.3
    public static final String primaryEpisodeIdentifierAssigningAuthority = "Homerton FIN";  // PID.19
    public static final String primaryPractitionerIdentifierTypeCode = "Personnel Primary Identifier";

    public static final int homertonXpdPrimaryCarePd1FieldNumber = 4;
}
