package org.endeavourhealth.hl7transform.homerton;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.Transform;
import org.endeavourhealth.hl7transform.homerton.parser.zsegments.*;
import org.endeavourhealth.hl7transform.homerton.pretransform.HomertonPreTransform;
import org.endeavourhealth.hl7transform.homerton.transforms.*;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.hl7.fhir.instance.model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HomertonAdtTransform implements Transform {

    private HashMap<String, Class<? extends Segment>> zSegments = new HashMap<>();

    public HomertonAdtTransform() {
        zSegments.put(HomertonSegmentName.ZAL, ZalSegment.class);
        zSegments.put(HomertonSegmentName.ZPI, ZpiSegment.class);
        zSegments.put(HomertonSegmentName.ZQA, ZqaSegment.class);
        zSegments.put(HomertonSegmentName.ZVI, ZviSegment.class);
    }

    public List<String> getSupportedSendingFacilities() {
        return Arrays.asList(new String[] { "HOMERTON" });
    }

    public HashMap<String, Class<? extends Segment>> getZSegments() {
        return zSegments;
    }

    public AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException {
        return HomertonPreTransform.preTransform(sourceMessage);
    }

    public Bundle transform(AdtMessage sourceMessage, Mapper mapper) throws Exception {
        Validate.notNull(sourceMessage);

        ResourceContainer targetResources = new ResourceContainer();

        new OrganizationTransform(mapper, targetResources)
                .createHomertonManagingOrganisation();

        new LocationTransform(mapper, targetResources)
                .createHomertonLocation();

        new MessageHeaderTransform(mapper, targetResources)
                .transform(sourceMessage);

        new PatientTransform(mapper, targetResources)
                .transform(sourceMessage);

        new EpisodeOfCareTransform(mapper, targetResources)
                .transform(sourceMessage);

        new EncounterTransform(mapper, targetResources)
                .transform(sourceMessage);

        return targetResources
                .orderByResourceType()
                .createBundle();
    }
}
