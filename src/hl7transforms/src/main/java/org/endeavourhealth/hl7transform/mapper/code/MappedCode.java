package org.endeavourhealth.hl7transform.mapper.code;

public class MappedCode {
    private MappedCodeAction action;
    private String code;
    private String system;
    private String term;

    public MappedCodeAction getAction() {
        return action;
    }

    public MappedCode setTargetAction(MappedCodeAction action) {
        this.action = action;
        return this;
    }

    public String getCode() {
        return code;
    }

    public MappedCode setCode(String code) {
        this.code = code;
        return this;
    }

    public String getSystem() {
        return system;
    }

    public MappedCode setSystem(String system) {
        this.system = system;
        return this;
    }

    public String getTerm() {
        return term;
    }

    public MappedCode setTerm(String term) {
        this.term = term;
        return this;
    }
}
