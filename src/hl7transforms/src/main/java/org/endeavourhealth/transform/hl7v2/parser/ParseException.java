package org.endeavourhealth.transform.hl7v2.parser;

public class ParseException extends Exception {
    final static long serialVersionUID = 1L;

    public ParseException() {
        super();
    }
    public ParseException(String message) {
        super(message);
    }
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
    public ParseException(Throwable cause) {
        super(cause);
    }
}
