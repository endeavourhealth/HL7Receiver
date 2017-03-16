package org.endeavourhealth.hl7transform.transform;

public class TransformException extends Exception {
    final static long serialVersionUID = 1L;

    public TransformException() {
        super();
    }
    public TransformException(String message) {
        super(message);
    }
    public TransformException(String message, Throwable cause) {
        super(message, cause);
    }
    public TransformException(Throwable cause) {
        super(cause);
    }
}
