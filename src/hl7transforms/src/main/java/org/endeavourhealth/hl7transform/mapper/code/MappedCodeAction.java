package org.endeavourhealth.hl7transform.mapper.code;

import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;

public enum MappedCodeAction {
    NOT_MAPPED_FAIL_TRANSFORMATION("F", "The code is not mapped - fail transformation of message"),
    NOT_MAPPED_EXCLUDE("X", "The code is not mapped - exclude the source code, system and term from transformed message"),
    NOT_MAPPED_INCLUDE_ONLY_SOURCE_TERM("S", "The code is not mapped - include only the source term in the transformed message"),
    MAPPED_INCLUDE("T", "The code is mapped - include the target code, system and term in the transformed message");

    private String identifier;
    private String description;

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    MappedCodeAction(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public static MappedCodeAction fromIdentifier(String identifier) throws MapperException {
        for (MappedCodeAction mappedCodeAction : MappedCodeAction.values())
            if (mappedCodeAction.getIdentifier().equals(identifier))
                return mappedCodeAction;

        throw new MapperException("Could not find CodeMappingAction value of " + identifier);
    }
}
