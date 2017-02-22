package org.endeavourhealth.transform.hl7v2.transform;

public class TransformException extends Exception {
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
