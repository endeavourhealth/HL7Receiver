package org.endeavourhealth.transform.hl7v2.mapper;

public class MapperException extends Exception {
    final static long serialVersionUID = 1L;

    public MapperException() {
        super();
    }
    public MapperException(String message) {
        super(message);
    }
    public MapperException(String message, Throwable cause) {
        super(message, cause);
    }
    public MapperException(Throwable cause) {
        super(cause);
    }
}
