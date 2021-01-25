package org.endeavourhealth.hl7receiver.sender;

public class EdsSenderHttpErrorResponseException extends Exception {
    static final long serialVersionUID = 1L;

    private EdsSenderResponse edsSenderResponse;

    public EdsSenderHttpErrorResponseException(String message, EdsSenderResponse edsSenderResponse) {
        super(message);
        this.edsSenderResponse = edsSenderResponse;
    }

    public EdsSenderResponse getEdsSenderResponse() {
        return edsSenderResponse;
    }
}