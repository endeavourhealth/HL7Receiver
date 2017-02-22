package org.endeavourhealth.hl7receiver.model.db;

import java.util.UUID;

public class DbErrorIdentifier {
    private int errorId;
    private UUID errorUuid;

    public int getErrorId() {
        return errorId;
    }

    public DbErrorIdentifier setErrorId(int errorId) {
        this.errorId = errorId;
        return this;
    }

    public UUID getErrorUuid() {
        return errorUuid;
    }

    public DbErrorIdentifier setErrorUuid(UUID errorUuid) {
        this.errorUuid = errorUuid;
        return this;
    }
}
