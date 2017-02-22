package org.endeavourhealth.transform.hl7v2.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Field {
    private static final int FIRST = 0;

    private String originalFieldText;    // originalFieldText may not reflect the current state of the field
    private Seperators seperators;
    protected List<GenericDatatype> genericDatatypes = new ArrayList<>();

    //////////////////  Constructors  //////////////////

    private Field() {
    }

    public Field(String fieldText, Seperators seperators) {
        Validate.notNull(fieldText);
        Validate.notNull(seperators);

        this.seperators = seperators;
        this.setAsString(fieldText);
    }

    //////////////////  Accessors  //////////////////

    public String getAsString() {
        return compose();
    }

    public List<String> getDatatypesAsString() {
        return genericDatatypes
            .stream()
            .map(t -> t.getAsString())
            .collect(Collectors.toList());
    }

    public String getComponentAsString(int componentNumber) {
        Component component = getComponent(componentNumber);

        if (component == null)
            return null;

        return component.getAsString();
    }

    public Component getComponent(int componentNumber) {
        GenericDatatype genericDatatype = getFirstGenericDatatype();

        if (genericDatatype == null)
            return null;

        return genericDatatype.getComponent(componentNumber);
    }

    private GenericDatatype getFirstGenericDatatype() {
        return Helpers.getSafely(this.genericDatatypes, FIRST);
    }

    public Datatype getDatatype() {
        return new Datatype(getFirstGenericDatatype());
    }

    public <T extends Datatype> T getDatatype(Class<T> datatype) {
        Validate.notNull(datatype);

        GenericDatatype genericDatatype = getFirstGenericDatatype();

        if (StringUtils.isEmpty(genericDatatype.getAsString()))        // should we create a datatype where the field is blank?
            return null;

        return Datatype.instantiate(datatype, getFirstGenericDatatype());
    }

    public List<Datatype> getDatatypes() {
        return this.genericDatatypes
                .stream()
                .map(t -> new Datatype(t))
                .collect(Collectors.toList());
    }

    public <T extends Datatype> List<T> getDatatypes(Class<T> datatype) {
        Validate.notNull(datatype);

        return this.genericDatatypes
                .stream()
                .filter(t -> !StringUtils.isEmpty(t.getAsString()))    // should we create a datatype where the field is blank?
                .map(t -> Datatype.instantiate(datatype, t))
                .collect(Collectors.toList());
    }

    public List<GenericDatatype> getGenericDatatypes() {
        return this.genericDatatypes;
    }

    public List<Component> getAllComponents() {
        return getGenericDatatypes()
                .stream()
                .flatMap(s -> s.getComponents().stream())
                .collect(Collectors.toList());
    }

    //////////////////  Setters  //////////////////

    public void setAsString(String fieldText) {
        this.originalFieldText = fieldText;
        this.parse();
    }

    public GenericDatatype addDatatype() {
        GenericDatatype datatype = new GenericDatatype("", this.seperators);
        this.genericDatatypes.add(datatype);
        return datatype;
    }

    //////////////////  Parsers  //////////////////

    private void parse() {
        this.genericDatatypes.clear();

        if (this.originalFieldText.equals(this.seperators.getMsh2Field())) {
            this.genericDatatypes.add(new GenericDatatype(this.originalFieldText, this.seperators));
            return;
        }

        List<String> fieldRepetitions = Helpers.split(this.originalFieldText, seperators.getRepetitionSeperator());

        for (String fieldRepetition : fieldRepetitions)
            this.genericDatatypes.add(new GenericDatatype(fieldRepetition, this.seperators));
    }

    //////////////////  Composers  //////////////////

    public String compose() {
        return String.join(this.seperators.getRepetitionSeperator(),
                this
                        .getGenericDatatypes()
                        .stream()
                        .map(t -> t.compose())
                        .collect(Collectors.toList()));
    }
}
