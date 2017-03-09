package org.endeavourhealth.hl7receiver.model.exceptions;

import org.endeavourhealth.hl7receiver.model.db.DbProcessingStatus;

public class HL7MessageProcessorException extends Hl7ReceiverException {
    static final long serialVersionUID = 1L;

    private DbProcessingStatus processingStatus;

    public HL7MessageProcessorException(DbProcessingStatus processingStatus, Throwable cause) {
        super(cause);
        this.processingStatus = processingStatus;
    }

    public DbProcessingStatus getProcessingStatus() {
        return processingStatus;
    }
}
