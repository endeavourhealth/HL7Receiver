package org.endeavourhealth.hl7parser.datatypes;

// extended common name
public interface XpnInterface {
    String getFamilyName();
    String getGivenName();
    String getMiddleName();
    String getSuffix();
    String getPrefix();
    String getDegree();
    String getNameTypeCode();
    String getNameRepresentationCode();
}