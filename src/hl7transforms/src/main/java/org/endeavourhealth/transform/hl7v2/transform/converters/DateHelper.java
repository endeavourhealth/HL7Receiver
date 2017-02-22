package org.endeavourhealth.transform.hl7v2.transform.converters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateHelper {
    public static Date fromLocalDateTime(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
