package org.endeavourhealth.hl7receiver.sender;

public class EdsSenderResponse {
    private int httpStatusCode;
    private String statusLine;
    private String responseBody;

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public EdsSenderResponse setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        return this;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public EdsSenderResponse setStatusLine(String statusLine) {
        this.statusLine = statusLine;
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public EdsSenderResponse setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }
}
