package org.endeavourhealth.hl7receiver.model.exceptions;

import org.endeavourhealth.hl7receiver.model.db.DbMessageStatus;

public class HL7MessageProcessorException extends Hl7ReceiverException {
    static final long serialVersionUID = 1L;

    private DbMessageStatus messageStatus;

    public HL7MessageProcessorException(DbMessageStatus messageStatus, Throwable cause) {
        super(messageStatus.getDescription(), cause);
        this.messageStatus = messageStatus;
    }

    public DbMessageStatus getMessageStatus() {
        return messageStatus;
    }
}
