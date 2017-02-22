package org.endeavourhealth.hl7receiver.model.exceptions;

public class Hl7ReceiverException extends Exception {
    static final long serialVersionUID = 1L;

    public Hl7ReceiverException() {
        super();
    }
    public Hl7ReceiverException(String message) {
        super(message);
    }
    public Hl7ReceiverException(String message, Throwable cause) {
        super(message, cause);
    }
    public Hl7ReceiverException(Throwable cause) {
        super(cause);
    }
}
