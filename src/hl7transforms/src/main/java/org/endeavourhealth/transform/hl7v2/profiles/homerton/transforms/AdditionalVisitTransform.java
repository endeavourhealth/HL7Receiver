package org.endeavourhealth.transform.hl7v2.profiles.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZviSegment;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.endeavourhealth.transform.hl7v2.transform.converters.CodeableConceptHelper;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Period;

public class AdditionalVisitTransform {

    public static void addAdditionalInformation(Encounter target, ZviSegment source) throws TransformException, ParseException {

        if (StringUtils.isNotBlank(source.getServiceCategory()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getServiceCategory()));

        if (StringUtils.isNotBlank(source.getAdmitMode()))
            target.addType(CodeableConceptHelper.getCodeableConceptFromString(source.getAdmitMode()));

        if (source.getAssignToLocationDate() != null) {

            target.addStatusHistory(
                    new Encounter.EncounterStatusHistoryComponent()
                            .setStatus(Encounter.EncounterState.ARRIVED)
                            .setPeriod(new Period()
                                    .setStart(source.getAssignToLocationDate().asDate())));
        }
    }
}
