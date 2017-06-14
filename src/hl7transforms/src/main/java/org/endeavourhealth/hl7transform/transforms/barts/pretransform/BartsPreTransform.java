package org.endeavourhealth.hl7transform.transforms.barts.pretransform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.*;
import org.endeavourhealth.hl7parser.messages.AdtMessage;

import java.util.List;

public class BartsPreTransform {
    public static AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException {
        Validate.notNull(sourceMessage);

        // remove all fields with only "" in them
        removeEmptyDoubleQuotes(sourceMessage);

        return sourceMessage;
    }

    private static void removeEmptyDoubleQuotes(AdtMessage sourceMessage) {
        List<Component> components = sourceMessage.getAllComponents();

        for (Component component : components)
            if (component.getAsString().equals("\"\""))
                component.setAsString("");
    }
}
