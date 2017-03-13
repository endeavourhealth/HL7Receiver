package org.endeavourhealth.hl7receiver.model.db;

public enum DbMessageStatus {
    MESSAGE_RECEIVED(0),
    MESSAGE_PROCESSING_STARTED(1),
    MESSAGE_PROCESSING_COMPLETE(9),
    TRANSFORM_FAILURE(-1),
    ENVELOPE_GENERATION_FAILURE(-2),
    SEND_FAILURE(-3),
    UNEXPECTED_ERROR(-9);

    private int dbMessageStatusType;

    DbMessageStatus(int dbMessageStatusType) {
        this.dbMessageStatusType = dbMessageStatusType;
    }

    public int getValue() {
        return dbMessageStatusType;
    }
}
