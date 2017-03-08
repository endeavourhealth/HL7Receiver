package org.endeavourhealth.hl7receiver.model.db;

public class DbCode {
    private String code;
    private String system;
    private String term;

    public String getCode() {
        return code;
    }

    public DbCode setCode(String code) {
        this.code = code;
        return this;
    }

    public String getSystem() {
        return system;
    }

    public DbCode setSystem(String system) {
        this.system = system;
        return this;
    }

    public String getTerm() {
        return term;
    }

    public DbCode setTerm(String term) {
        this.term = term;
        return this;
    }
}
