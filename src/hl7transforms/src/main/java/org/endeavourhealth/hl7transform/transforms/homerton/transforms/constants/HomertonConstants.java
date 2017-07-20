package org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants;

import java.util.Arrays;
import java.util.List;

public class HomertonConstants {

    public static final String odsCode = "RQX";

    public static final String sendingFacility = "HOMERTON";  //MSH.4
    public static final List<String> servicingFacilities = Arrays.asList("HOMERTON UNIVER"); // "NEWHAM GENERAL"  // PV1.39
    public static final List<String> locationFacilities = servicingFacilities;   // PV1.3.4

    public static final String odsSiteCodeHomerton = "RQXM1";
    public static final String odsSiteCodeStLeonards = "RQX20";
//    public static final String odsSiteCodeNewham = "RGCNH";

    public static final String locationBuildingHomerton = "HOMERTON UH";                // PV1.3.7
    public static final String locationBuildingStLeonards = "St Leonard's Hospital";    // PV1.3.7
//    public static final String locationBuildingNewham = "Newham General";               // PV1.3.7

    public static final String primaryPatientIdentifierTypeCode = "CNN";  // PID.3
    public static final String primaryEpisodeIdentifierAssigningAuthority = "Homerton FIN";  // PID.19
    public static final String primaryPractitionerIdentifierTypeCode = "Personnel Primary Identifier";

    public static final int homertonXpdPrimaryCarePd1FieldNumber = 4;

    public static final String homertonDefaultPhoneNumberValue = "(000)000-0000";
}
