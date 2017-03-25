package org.endeavourhealth.hl7transform.common.converters;

import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.ParseException;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DateType;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;
import org.hl7.fhir.instance.model.Type;

public class DateConverter {

    public static Type getDateType(Hl7DateTime dateTime) throws ParseException {
        switch (dateTime.getPrecision()) {
            case "yyyy":
            case "yyyy-MM":
                return new DateType(dateTime.getLocalDateTime().format(dateTime.getFormatter()));
            case "yyyyMMdd":
                return new DateType(dateTime.asDate());
            case "yyyyMMddHHmm":
            case "yyyyMMddHHmmss":
            case "yyyyMMddHHmmss.S":
            case "yyyyMMddHHmmss.SS":
            case "yyyyMMddHHmmss.SSS":
            case "yyyyMMddHHmmss.SSSS": {
                DateTimeType dtt = new DateTimeType();
                dtt.setValue(dateTime.asDate(), TemporalPrecisionEnum.MILLI);
                return dtt;
            }
            default: throw new ParseException("Could not parse date time");
        }
    }
}
