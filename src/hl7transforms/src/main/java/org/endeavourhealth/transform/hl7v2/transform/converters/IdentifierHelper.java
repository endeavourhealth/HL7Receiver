package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.endeavourhealth.transform.hl7v2.parser.datatypes.Cx;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xcn;

import java.time.LocalDateTime;
import java.util.UUID;


public class IdentifierHelper {

    public static String generateId(String uniqueString, String identifierString) {
        return UUID.nameUUIDFromBytes((identifierString + uniqueString).getBytes()).toString();
    }

    public static String generateId(String uniqueString) {
        return UUID.nameUUIDFromBytes(uniqueString.getBytes()).toString();
    }

    public static String generateId(Xcn xcn) {
        if (xcn == null)
            return UUID.randomUUID().toString();

        return UUID.nameUUIDFromBytes(xcn.getAsString().getBytes()).toString();
    }

    public static String generateId(Cx cx) {
        if (cx == null)
            return UUID.randomUUID().toString();

        return UUID.nameUUIDFromBytes(cx.getAsString().getBytes()).toString();
    }

    public static String generateId(LocalDateTime dateTime) {
        if (dateTime == null)
            return UUID.randomUUID().toString();

        return UUID.nameUUIDFromBytes(dateTime.toString().getBytes()).toString();
    }
}
