package org.endeavourhealth.hl7receiver.model.db;

public enum DbProcessingContentType {
    FHIR(1),
    ONWARD_REQUEST_MESSAGE(2),
    ONWARD_RESPONSE_MESSAGE(3);

    private int dbProcessingContentType;

    DbProcessingContentType(int dbProcessingContentType) {
        this.dbProcessingContentType = dbProcessingContentType;
    }

    public int getValue() {
        return this.dbProcessingContentType;
    }
}
