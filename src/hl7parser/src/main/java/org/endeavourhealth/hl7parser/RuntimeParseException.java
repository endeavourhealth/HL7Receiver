package org.endeavourhealth.hl7parser;

public class RuntimeParseException extends RuntimeException {
    final static long serialVersionUID = 1L;

    public RuntimeParseException() {
        super();
    }
    public RuntimeParseException(String message) {
        super(message);
    }
    public RuntimeParseException(String message, Throwable cause) {
        super(message, cause);
    }
    public RuntimeParseException(Throwable cause) {
        super(cause);
    }
}
