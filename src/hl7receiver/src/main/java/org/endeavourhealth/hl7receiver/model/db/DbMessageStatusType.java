package org.endeavourhealth.hl7receiver.model.db;

public enum DbMessageStatusType {
    RECEIVED(1),
    RETRIEVED_FOR_PROCESSING(2),
    TRANSFORMED(3),
    TRANSFORM_ERROR(-3),
    NOTIFICATION_CREATED(4),
    NOTIFICATION_CREATION_ERROR(-4),
    NOTIFICATION_SENT(5),
    NOTIFICATION_SEND_ERROR(-5),
    UNEXPECTED_PROCESSING_ERROR(-9);

    private int dbMessageStatusType;

    DbMessageStatusType(int dbMessageStatusType) {
        this.dbMessageStatusType = dbMessageStatusType;
    }

    public int getValue() {
        return dbMessageStatusType;
    }
}
