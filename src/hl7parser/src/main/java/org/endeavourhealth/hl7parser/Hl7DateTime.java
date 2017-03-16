package org.endeavourhealth.hl7parser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Hl7DateTime {
    private LocalDateTime localDateTime;
    private String precision;
    private DateTimeFormatter formatter;
    private Boolean hasTimeComponent;

    public Hl7DateTime(String dateTime) throws ParseException{
        this.localDateTime = DateParser.parse(dateTime);
        this.precision = DateParser.getPattern(dateTime);
        this.formatter = DateTimeFormatter.ofPattern(this.precision);
        this.hasTimeComponent = (dateTime.length() > 8);
    }

    public Date asDate() {
        return Date.from(this.localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public String getPrecision() {
        return precision;
    }

    public boolean hasTimeComponent() {
        return this.hasTimeComponent;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }
}
