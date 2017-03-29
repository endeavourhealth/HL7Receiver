package org.endeavourhealth.hl7transform.mapper.code;

public class CodeMapping {
    private CodeMappingAction action;
    private String code;
    private String system;
    private String term;

    public CodeMappingAction getAction() {
        return action;
    }

    public CodeMapping setTargetAction(CodeMappingAction action) {
        this.action = action;
        return this;
    }

    public String getCode() {
        return code;
    }

    public CodeMapping setCode(String code) {
        this.code = code;
        return this;
    }

    public String getSystem() {
        return system;
    }

    public CodeMapping setSystem(String system) {
        this.system = system;
        return this;
    }

    public String getTerm() {
        return term;
    }

    public CodeMapping setTerm(String term) {
        this.term = term;
        return this;
    }
}
