package org.endeavourhealth.hl7receiver.engine;

import org.endeavourhealth.common.postgres.PgStoredProcException;
import org.endeavourhealth.hl7receiver.model.db.DbProcessingContentType;

public interface HL7ContentSaver {
    void save(DbProcessingContentType contentType, String content) throws PgStoredProcException;
}
