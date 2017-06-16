package org.endeavourhealth.hl7receiver.model.exceptions;

public class TransientMessageProcessingException extends Hl7ReceiverException {
    static final long serialVersionUID = 1L;

    public TransientMessageProcessingException() {
        super();
    }
    public TransientMessageProcessingException(String message) {
        super(message);
    }
    public TransientMessageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    public TransientMessageProcessingException(Throwable cause) {
        super(cause);
    }
}
