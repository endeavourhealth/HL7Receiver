package org.endeavourhealth.hl7transform.common.transform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.Hl7DateTime;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.DateTimeHelper;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Period;

import java.util.ArrayList;
import java.util.List;

public class EpisodeOfCareCommon {

    public static List<Cx> getAllEpisodeIdentifiers(AdtMessage source) {
        Validate.notNull(source.getPidSegment());
        Validate.notNull(source.getPv1Segment());

        List<Cx> episodeIdentifiers = new ArrayList<>();

        if (source.getPidSegment().getPatientAccountNumber() != null)
            episodeIdentifiers.add(source.getPidSegment().getPatientAccountNumber());

        if (source.getPv1Segment().getVisitNumber() != null)
            episodeIdentifiers.add(source.getPv1Segment().getVisitNumber());

        if (source.getPv1Segment().getAlternateVisitID() != null)
            episodeIdentifiers.add(source.getPv1Segment().getAlternateVisitID());

        return episodeIdentifiers;
    }

    public static String getEpisodeIdentifierValueByTypeCode(List<Cx> cxs, String episodeIdentifierTypeCode) {
        return cxs
                .stream()
                .filter(t -> episodeIdentifierTypeCode.equals(t.getIdentifierTypeCode()))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());
    }

    public static String getEpisodeIdentifierValueByTypeCode(AdtMessage source, String episodeIdentifierTypeCode) {
        return getEpisodeIdentifierValueByTypeCode(EpisodeOfCareCommon.getAllEpisodeIdentifiers(source), episodeIdentifierTypeCode);
    }

    public static String getEpisodeIdentifierValueByAssigningAuthority(AdtMessage source, String episodeIdentifierAssigningAuthority) {
        return getAllEpisodeIdentifiers(source)
                .stream()
                .filter(t -> episodeIdentifierAssigningAuthority.equals(t.getAssigningAuthority()))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());
    }

    public static void setStatusAndPeriod(EpisodeOfCare target, String accountStatus, Hl7DateTime admitDate, Hl7DateTime dischargeDate, Hl7DateTime eventRecordedDate, Mapper mapper) throws TransformException, ParseException, MapperException {

        EpisodeOfCare.EpisodeOfCareStatus episodeOfCareStatus = mapper.getCodeMapper().mapAccountStatus2(accountStatus);

        if (episodeOfCareStatus != null)
            target.setStatus(episodeOfCareStatus);

        Hl7DateTime endDate = dischargeDate;

        if (isClosedStatus(episodeOfCareStatus) && (endDate == null))
            endDate = eventRecordedDate;

        Period period = DateTimeHelper.createPeriod(admitDate, endDate);

        if (period != null)
            target.setPeriod(period);
    }

    public static boolean isClosedStatus(EpisodeOfCare.EpisodeOfCareStatus episodeOfCareStatus) {
        if (episodeOfCareStatus == null)
            return false;

        switch (episodeOfCareStatus) {
            case ONHOLD:
            case FINISHED:
            case CANCELLED: return true;
            default: return false;
        }
    }
}
