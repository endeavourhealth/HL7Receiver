package org.endeavourhealth.hl7receiver.model.db;

public enum DbMessageStatus {
    MESSAGE_RECEIVED(0, "Message received"),
    MESSAGE_PROCESSING_STARTED(1, "Message processing started"),
    MESSAGE_PROCESSING_COMPLETE(9, "Message processing complete"),
    TRANSFORM_FAILURE(-1, "Transform failure"),
    ENVELOPE_GENERATION_FAILURE(-2, "Envelope generation failure"),
    SEND_FAILURE(-3, "Send failure"),
    UNEXPECTED_ERROR(-9, "Unexpected error");

    private int dbMessageStatusType;
    private String dbMessageDescription;

    DbMessageStatus(int dbMessageStatusType, String dbMessageDescription) {
        this.dbMessageStatusType = dbMessageStatusType;
        this.dbMessageDescription = dbMessageDescription;
    }

    public int getValue() {
        return dbMessageStatusType;
    }
    public String getDescription() { return dbMessageDescription; }
}
