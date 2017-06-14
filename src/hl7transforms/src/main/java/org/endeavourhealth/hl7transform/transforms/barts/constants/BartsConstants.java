package org.endeavourhealth.hl7transform.transforms.barts.constants;

public class BartsConstants {
    public static final String organisationName = "Barts Health NHS Trust";
    public static final String odsCode = "R1H";

    public static final String addressLine1 = "The Royal London Hospital";
    public static final String addressLine2 = "Whitechapel";
    public static final String town = "London";
    public static final String addressPostcode = "E1 1BB";

    public static final String sendingFacility = "2.16.840.1.113883.3.2540";  //MSH.4

    public static final String primaryPatientIdentifierTypeCode = "2.16.840.1.113883.3.2540.1";  // PID.3

    /*
    public static final String servicingFacility = "HOMERTON UNIVER";  // PV1.39
    public static final String locationFacility = servicingFacility;   // PV1.3.4
    public static final String locationBuilding = "HOMERTON UH";       // PV1.3.7
    public static final String primaryEpisodeIdentifierAssigningAuthority = "Homerton FIN";  // PID.19
    public static final String primaryPractitionerIdentifierTypeCode = "Personnel Primary Identifier";

    public static final int bartsXpdPrimaryCarePd1FieldNumber = 4;*/

    public class MileEndConstants {
        public static final String locationName = "Mile End Hospital";
        public static final String odsSiteCode = "R1H13";

        public static final String addressLine = "Bancroft Road";
        public static final String addressCity = "London";
        public static final String addressPostcode = "E1 4DG";

        //public static final String locationBuilding = "";    // PV1.3.7
    }

    public class NewhamConstants {
        public static final String locationName = "Newham General Hospital";
        public static final String odsSiteCode = "R1HNH";

        public static final String addressLine = "Glen Road";
        public static final String addressCity = "London";
        public static final String addressPostcode = "E13 8SL";

        //public static final locationBuilding = ""     // PV1.3.7
    }

    public class RoyalLondonConstants {
        public static final String locationName = "The Royal London Hospital";
        public static final String odsSiteCode = "R1H12";

        public static final String addressLine = "Whitechapel";
        public static final String town = "London";
        public static final String addressPostcode = "E1 1BB";

    }

    public class StBartholomewsConstants {

    }

    public class WhippsCrossConstants {

    }
}
