package org.endeavourhealth.transform.hl7v2.mapper;

public abstract class CodeMapper {
    private CodeMapper() {
    }

    public CodeMapper(String originatingSystemId) {

    }

    public abstract Code map(Code code, String contextId);
}