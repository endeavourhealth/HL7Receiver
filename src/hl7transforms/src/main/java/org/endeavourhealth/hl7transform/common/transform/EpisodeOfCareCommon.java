package org.endeavourhealth.hl7transform.common.transform;

import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.messages.AdtMessage;

import java.util.ArrayList;
import java.util.List;

public class EpisodeOfCareCommon {

    public static List<Cx> getAllEpisodeIdentifiers(AdtMessage source) {
        List<Cx> episodeIdentifiers = new ArrayList<>();

        if (source.getPidSegment().getPatientAccountNumber() != null)
            episodeIdentifiers.add(source.getPidSegment().getPatientAccountNumber());

        if (source.getPv1Segment().getVisitNumber() != null)
            episodeIdentifiers.add(source.getPv1Segment().getVisitNumber());

        if (source.getPv1Segment().getAlternateVisitID() != null)
            episodeIdentifiers.add(source.getPv1Segment().getAlternateVisitID());

        return episodeIdentifiers;
    }

    public static String getEpisodeIdentifierValueByTypeCode(AdtMessage source, String episodeIdentifierTypeCode) {
        return EpisodeOfCareCommon.getAllEpisodeIdentifiers(source)
                .stream()
                .filter(t -> episodeIdentifierTypeCode.equals(t.getIdentifierTypeCode()))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());
    }

    public static String getEpisodeIdentifierValueByAssigningAuthority(AdtMessage source, String episodeIdentifierAssigningAuthority) {
        return getAllEpisodeIdentifiers(source)
                .stream()
                .filter(t -> episodeIdentifierAssigningAuthority.equals(t.getAssigningAuthority()))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());
    }
}
