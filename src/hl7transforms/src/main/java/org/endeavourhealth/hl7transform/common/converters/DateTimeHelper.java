package org.endeavourhealth.hl7transform.common.converters;

import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.common.converters.DateConverter;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Period;

public class DateTimeHelper {
    public static Period createPeriod(Hl7DateTime start, Hl7DateTime end) throws ParseException {
        if ((start == null) && (end == null))
            return null;

        Period period = new Period();

        if (start != null)
            period.setStartElement((DateTimeType)DateConverter.getDateType(start));

        if (end != null)
            period.setEndElement((DateTimeType)DateConverter.getDateType(end));

        return period;
    }
}
