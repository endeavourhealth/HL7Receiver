package org.endeavourhealth.hl7transform.common.transform;

import org.endeavourhealth.common.utility.StreamExtension;
import org.hl7.fhir.instance.model.Identifier;

import java.util.List;

public class PractitionerCommon {
    public static boolean hasIdentifierWithSystem(List<Identifier> identifiers, String system) {
        return (getIdentifierWithSystem(identifiers, system) != null);
    }

    public static String getIdentifierValue(List<Identifier> identifiers, String system) {
        Identifier identifier = getIdentifierWithSystem(identifiers, system);

        if (identifier == null)
            return null;

        return identifier.getValue();
    }

    public static Identifier getIdentifierWithSystem(List<Identifier> identifiers, String system) {
        if (identifiers == null)
            return null;

        return identifiers
                .stream()
                .filter(t -> t.getSystem().equalsIgnoreCase(system))
                .collect(StreamExtension.firstOrNullCollector());
    }
}
