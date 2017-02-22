package org.endeavourhealth.transform.hl7v2.profiles.homerton;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZviSegment;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.endeavourhealth.transform.hl7v2.transform.converters.CodeableConceptHelper;
import org.endeavourhealth.transform.hl7v2.transform.converters.DateHelper;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Period;

public class AdditionalVisitTransform {

    public static Encounter addAdditionalInformation(Encounter target, ZviSegment source) throws TransformException, ParseException {

        if (StringUtils.isNotBlank(source.getServiceCategory()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getServiceCategory()));

        if (StringUtils.isNotBlank(source.getAdmitMode()))
        target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getAdmitMode()));

        if (source.getAssignToLocationDate() != null) {
            Encounter.EncounterStatusHistoryComponent shc = new Encounter.EncounterStatusHistoryComponent();

            shc.setStatus(Encounter.EncounterState.ARRIVED);
            Period period = new Period();
            period.setStart(DateHelper.fromLocalDateTime(source.getAssignToLocationDate()));
            shc.setPeriod(period);
            target.addStatusHistory(shc);
        }

        return target;
    }
}
