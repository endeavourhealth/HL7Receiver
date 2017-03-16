package org.endeavourhealth.hl7parser.datatypes;

public interface CxInterface {
    String getId();
    String getCheckDigit();
    String getCheckDigitCodeScheme();
    String getAssigningAuthority();
    String getIdentifierTypeCode();
    String getAssigningFacility();
}
