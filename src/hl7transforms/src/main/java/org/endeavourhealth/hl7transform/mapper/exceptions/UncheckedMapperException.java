package org.endeavourhealth.hl7transform.mapper.exceptions;

public class UncheckedMapperException extends RuntimeException {
    final static long serialVersionUID = 1L;

    public UncheckedMapperException() {
        super();
    }
    public UncheckedMapperException(String message) {
        super(message);
    }
    public UncheckedMapperException(String message, Throwable cause) {
        super(message, cause);
    }
    public UncheckedMapperException(Throwable cause) {
        super(cause);
    }
}
