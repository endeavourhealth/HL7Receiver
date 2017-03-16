package org.endeavourhealth.hl7parser;

import org.apache.commons.lang3.Validate;

public class Component {
    private static final int FIRST = 0;

    private String originalComponentText;    // originalComponentText may not reflect the current state of the component
    private Seperators seperators;
    protected String componentText;

    //////////////////  Constructors  //////////////////

    private Component() {
    }

    public Component(String componentText, Seperators seperators) {
        Validate.notNull(componentText);
        Validate.notNull(seperators);

        this.originalComponentText = componentText;
        this.seperators = seperators;

        this.parse();
    }

    //////////////////  Accessors  //////////////////

    public String getAsString() {
        return this.compose();
    }

    //////////////////  Setters  //////////////////

    public void setAsString(String componentText) {
        this.componentText = componentText;
    }

    //////////////////  Parsers  //////////////////

    private void parse() {
        this.componentText = originalComponentText;
    }

    //////////////////  Composers  //////////////////

    public String compose() {
        return this.componentText;
    }
}
