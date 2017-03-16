package org.endeavourhealth.hl7parser;

import java.util.Arrays;
import java.util.List;

public class Seperators {
    private static final String CR = "\r";

    private String lineSeperator = CR;
    private String fieldSeperator;
    private String componentSeperator;
    private String repetitionSeperator;
    private String escapeCharacter;
    private String subcomponentSeperator;

    public String getLineSeperator() {
        return lineSeperator;
    }

    public String getFieldSeperator() {
        return fieldSeperator;
    }

    public Seperators setFieldSeperator(String fieldSeperator) {
        this.fieldSeperator = fieldSeperator;
        return this;
    }

    public String getComponentSeperator() {
        return componentSeperator;
    }

    public Seperators setComponentSeperator(String componentSeperator) {
        this.componentSeperator = componentSeperator;
        return this;
    }

    public String getRepetitionSeperator() {
        return repetitionSeperator;
    }

    public Seperators setRepetitionSeperator(String repetitionSeperator) {
        this.repetitionSeperator = repetitionSeperator;
        return this;
    }

    public String getEscapeCharacter() {
        return escapeCharacter;
    }

    public Seperators setEscapeCharacter(String escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
        return this;
    }

    public String getSubcomponentSeperator() {
        return subcomponentSeperator;
    }

    public Seperators setSubcomponentSeperator(String subcomponentSeperator) {
        this.subcomponentSeperator = subcomponentSeperator;
        return this;
    }

    public String getMsh2Field() {
        return this.getComponentSeperator() +
                this.getRepetitionSeperator() +
                this.getEscapeCharacter() +
                this.getSubcomponentSeperator();
    }

    public boolean areSeperatorsUnique() {
        List<String> seperators = Arrays.asList(this.fieldSeperator, this.componentSeperator, this.repetitionSeperator, this.escapeCharacter, this.subcomponentSeperator);
        return (seperators.stream().distinct().count() == seperators.size());
    }
}
