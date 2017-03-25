package org.endeavourhealth.hl7parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;

public class Datatype {
    protected GenericDatatype datatype;

    public Datatype(GenericDatatype datatype) {
        Validate.notNull(datatype);

        this.datatype = datatype;
    }

    public Component getComponent(int componentNumber) {
        return this.datatype.getComponent(componentNumber);
    }

    public String getComponentAsString(int componentNumber) {
        Component component = getComponent(componentNumber);

        if (component == null)
            return null;

        return component.getAsString();
    }

    public LocalDateTime getComponentAsDate(int componentNumber) throws ParseException {
        String component = this.datatype.getComponentAsString(componentNumber);

        if (StringUtils.isBlank(component))
            return null;

        return DateParser.parse(component);
    }

    public String getAsString() {
        return this.datatype.getAsString();
    }

    public boolean allComponentsAreBlank() {
        return this.datatype.allComponentsAreBlank();
    }

    public static <T extends Datatype> T instantiate(Class<T> dt, GenericDatatype datatype) {
        try {
            Constructor<T> constructor = dt.getConstructor(GenericDatatype.class);
            return constructor.newInstance(datatype);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate " + dt.getName(), e);
        }
    }
}
