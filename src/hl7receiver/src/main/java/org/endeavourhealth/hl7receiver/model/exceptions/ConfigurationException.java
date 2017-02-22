package org.endeavourhealth.hl7receiver.model.exceptions;

public class ConfigurationException extends Hl7ReceiverException {
    static final long serialVersionUID = 1L;

    public ConfigurationException() {
        super();
    }
    public ConfigurationException(String message) {
        super(message);
    }
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
