package org.endeavourhealth.transform.hl7v2.parser;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GenericDatatype {
    private static final int FIRST = 0;

    private String originalDatatypeText;    // originalDatatypeText may not reflect the current state of the datatype
    private Seperators seperators;
    protected List<Component> components = new ArrayList<>();

    //////////////////  Constructors  //////////////////

    private GenericDatatype() {
    }

    public GenericDatatype(String datatypeText, Seperators seperators) {
        Validate.notNull(datatypeText);
        Validate.notNull(seperators);

        this.originalDatatypeText = datatypeText;
        this.seperators = seperators;

        this.parse();
    }

    //////////////////  Accessors  //////////////////

    public String getAsString() {
        return this.compose();
    }

    public Component getComponent(int componentNumber) {
        int componentIndex = componentNumber - 1;
        return Helpers.getSafely(this.components, componentIndex);
    }

    public String getComponentAsString(int componentNumber) {
        Component component = getComponent(componentNumber);

        if (component == null)
            return null;

        return component.getAsString();
    }

    public List<Component> getComponents() {
        return this.components;
    }

    //////////////////  Setters  //////////////////

    public void setComponentAsString(int componentNumber, String componentText) {
        Component component = getComponent(componentNumber);

        if (component != null) {
            component.setAsString(componentText);
        } else {
            int componentIndex = componentNumber - 1;
            int maxCurrentComponentIndex = (this.components.size() - 1);

            if (component == null) {
                for (int i = 0; i <= componentIndex; i++) {
                    if (i > maxCurrentComponentIndex) {
                        if (i == componentIndex)
                            this.components.add(new Component(componentText, this.seperators));
                        else
                            this.components.add(new Component("", this.seperators));
                    }
                }
            }
        }
    }

    //////////////////  Parsers  //////////////////

    private void parse() {
        if (this.originalDatatypeText.equals(this.seperators.getMsh2Field())) {
            this.components.add(new Component(this.originalDatatypeText, this.seperators));
            return;
        }

        this.components = Helpers.split(this.originalDatatypeText, seperators.getComponentSeperator())
                .stream()
                .map(t -> new Component(t, this.seperators))
                .collect(Collectors.toList());
    }

    //////////////////  Composers  //////////////////

    public String compose() {
        return String.join(this.seperators.getComponentSeperator(),
                this
                        .getComponents()
                        .stream()
                        .map(t -> t.compose())
                        .collect(Collectors.toList()));
    }
}
