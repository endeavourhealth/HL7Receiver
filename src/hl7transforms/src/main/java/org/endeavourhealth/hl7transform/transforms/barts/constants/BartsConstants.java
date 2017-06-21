package org.endeavourhealth.hl7transform.transforms.barts.constants;

public class BartsConstants {
    public static final String odsCode = "R1H";

    public static final String sendingFacility = "2.16.840.1.113883.3.2540";  //MSH.4

    public static final String primaryPatientIdentifierAssigningAuthority = "2.16.840.1.113883.3.2540.1";  // PID.3
    public static final String primaryEpisodeIdentifierTypeCode = "VISITID";  // PV1.19
    public static final String primaryPractitionerIdentifierTypeCode = "XXXDoNotUse";

//    public static final String locationFacility = servicingFacility;   // PV1.3.4
//    public static final String locationBuilding = "HOMERTON UH";       // PV1.3.7
//
//    public static final String primaryPractitionerIdentifierTypeCode = "Personnel Primary Identifier";
//
//    public static final int bartsXpdPrimaryCarePd1FieldNumber = 4;

}
