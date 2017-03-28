package org.endeavourhealth.hl7transform.mapper;

import java.util.Arrays;

public enum CodeMappingAction {
    FAIL_TRANSFORMATION("F", "Fail transformation of message"),
    EXCLUDE("E", "Exclude (source or target) code, system and term from transformed message"),
    INCLUDE_ONLY_SOURCE_TERM("S", "Include only the source term in the transformed message"),
    INCLUDE("T", "Include the target code, system and term in the transformed message");

    private String identifier;
    private String description;

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    CodeMappingAction(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public static CodeMappingAction fromIdentifier(String identifier) throws MapperException {
        for (CodeMappingAction codeMappingAction : CodeMappingAction.values())
            if (codeMappingAction.getIdentifier().equals(identifier))
                return codeMappingAction;

        throw new MapperException("Could not find CodeMappingAction value of " + identifier);
    }
}
