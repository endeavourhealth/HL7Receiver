package org.endeavourhealth.transform.hl7v2.transform;

import org.endeavourhealth.transform.hl7v2.parser.DateParser;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DateType;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;
import org.hl7.fhir.instance.model.Type;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Hl7DateTime {
    LocalDateTime localDateTime;
    String precision;
    DateTimeFormatter formatter;
    Boolean hasTimeComponent;

    public Hl7DateTime(String dateTime) throws ParseException{
        this.localDateTime = DateParser.parse(dateTime);
        this.precision = DateParser.getPattern(dateTime);
        this.formatter = DateTimeFormatter.ofPattern(this.precision);
        this.hasTimeComponent = dateTime.length() > 8;
    }

    public Date asDate() {
        return Date.from(this.localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public Type getDateTimeType() throws ParseException {
        switch (this.precision) {
            case "yyyy":
            case "yyyy-MM":
                return new DateType(this.localDateTime.format(this.formatter));
            case "yyyyMMdd":
                return new DateType(this.asDate());
            case "yyyyMMddHHmm":
            case "yyyyMMddHHmmss":
            case "yyyyMMddHHmmss.S":
            case "yyyyMMddHHmmss.SS":
            case "yyyyMMddHHmmss.SSS":
            case "yyyyMMddHHmmss.SSSS": {
                DateTimeType dtt = new DateTimeType();
                dtt.setValue(this.asDate(), TemporalPrecisionEnum.MILLI);
                return dtt;
            }
            default: throw new ParseException("Could not parse date time");
        }
    }
}
