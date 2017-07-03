package org.endeavourhealth.hl7transform.transforms.homerton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.SegmentName;
import org.endeavourhealth.hl7transform.Transform;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.transforms.homerton.parser.zsegments.*;
import org.endeavourhealth.hl7transform.transforms.homerton.pretransform.HomertonPreTransform;
import org.endeavourhealth.hl7transform.transforms.homerton.transforms.*;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants.HomertonConstants;
import org.hl7.fhir.instance.model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HomertonAdtTransform extends Transform {

    private HashMap<String, Class<? extends Segment>> zSegments = new HashMap<>();

    public HomertonAdtTransform() {
        zSegments.put(HomertonSegmentName.ZAL, ZalSegment.class);
        zSegments.put(HomertonSegmentName.ZPI, ZpiSegment.class);
        zSegments.put(HomertonSegmentName.ZQA, ZqaSegment.class);
        zSegments.put(HomertonSegmentName.ZVI, ZviSegment.class);
    }

    public List<String> getSupportedSendingFacilities() {
        return Arrays.asList(new String[] { HomertonConstants.sendingFacility });
    }

    public HashMap<String, Class<? extends Segment>> getZSegments() {
        return zSegments;
    }

    public AdtMessage preTransform(AdtMessage sourceMessage) throws Exception {
        Validate.notNull(sourceMessage);
        validateSendingFacility(sourceMessage);

        return HomertonPreTransform.preTransform(sourceMessage);
    }

    public Bundle transform(AdtMessage sourceMessage, Mapper mapper) throws Exception {
        Validate.notNull(sourceMessage);
        validateSendingFacility(sourceMessage);
        validateSegmentCounts(sourceMessage);

        ResourceContainer targetResources = new ResourceContainer();

        ///////////////////////////////////////////////////////////////////////////
        // create main hospital organisation
        //
        HomertonOrganizationTransform homertonOrganizationTransform = new HomertonOrganizationTransform(mapper, targetResources);
        Organization mainHospitalOrganisation = homertonOrganizationTransform.createHomertonManagingOrganisation(sourceMessage);
        targetResources.addResource(mainHospitalOrganisation, ResourceTag.MainHospitalOrganisation);

        ///////////////////////////////////////////////////////////////////////////
        // create main hospital location
        //
        HomertonLocationTransform homertonLocationTransform = new HomertonLocationTransform(mapper, targetResources);
        Location location = homertonLocationTransform.createHomertonHospitalLocation();
        targetResources.addResource(location, ResourceTag.MainHospitalLocation);

        ///////////////////////////////////////////////////////////////////////////
        // create usual gp organisation
        //
        Organization mainGPOrganisation = homertonOrganizationTransform.createMainPrimaryCareProviderOrganisation(sourceMessage);

        if (mainGPOrganisation != null)
            targetResources.addResource(mainGPOrganisation, ResourceTag.MainPrimaryCareProviderOrganisation);

        ///////////////////////////////////////////////////////////////////////////
        // create usual gp practitioner
        //
        HomertonPractitionerTransform homertonPractitionerTransform = new HomertonPractitionerTransform(mapper, targetResources);
        Practitioner mainGPPractitioner = homertonPractitionerTransform.createMainPrimaryCareProviderPractitioner(sourceMessage);

        if (mainGPPractitioner != null)
            targetResources.addResource(mainGPPractitioner, ResourceTag.MainPrimaryCareProviderPractitioner);

        ///////////////////////////////////////////////////////////////////////////
        // create patient
        //
        HomertonPatientTransform homertonPatientTransform = new HomertonPatientTransform(mapper, targetResources);
        Patient patient = homertonPatientTransform.transform(sourceMessage);
        targetResources.addResource(patient, ResourceTag.PatientSubject);

        ///////////////////////////////////////////////////////////////////////////
        // create episode of care
        //
        // and any associated organisations (/services), practitioners, locations
        //
        HomertonEpisodeOfCareTransform homertonEpisodeOfCareTransform = new HomertonEpisodeOfCareTransform(mapper, targetResources);
        EpisodeOfCare episodeOfCare = homertonEpisodeOfCareTransform.transform(sourceMessage);

        if (episodeOfCare != null)
            targetResources.addResource(episodeOfCare);

        ///////////////////////////////////////////////////////////////////////////
        // create encounter
        //
        HomertonEncounterTransform homertonEncounterTransform = new HomertonEncounterTransform(mapper, targetResources);
        Encounter encounter = homertonEncounterTransform.transform(sourceMessage);

        if (encounter != null)
            targetResources.addResource(encounter);

        ///////////////////////////////////////////////////////////////////////////
        // create message header
        //
        HomertonMessageHeaderTransform homertonMessageHeaderTransform = new HomertonMessageHeaderTransform(mapper, targetResources);
        MessageHeader messageHeader = homertonMessageHeaderTransform.transform(sourceMessage);
        targetResources.addResource(messageHeader);

        ///////////////////////////////////////////////////////////////////////////
        // create bundle
        //
        return targetResources
                .orderByResourceType()
                .createBundle();
    }

    private void validateSendingFacility(AdtMessage sourceMessage) throws TransformException {
        Validate.notNull(sourceMessage.getMshSegment());

        if (!supportsSendingFacility(sourceMessage.getMshSegment().getSendingFacility()))
            throw new TransformException("Sending facility of " + sourceMessage.getMshSegment().getSendingFacility() + " not recognised");
    }

    private void validateSegmentCounts(AdtMessage sourceMessage) throws TransformException {
        // improve segment count definitions

        String messageType = StringUtils.trim(sourceMessage.getMshSegment().getMessageType());
        List<String> swapMessageTypes = Arrays.asList("ADT^A17");
        List<String> mergeMessageTypes = Arrays.asList("ADT^A34", "ADT^A35", "ADT^A44");

        validateExactlyOneSegment(sourceMessage, SegmentName.MSH);
        validateExactlyOneSegment(sourceMessage, SegmentName.EVN);

        if (!swapMessageTypes.contains(messageType))
            validateExactlyOneSegment(sourceMessage, SegmentName.PID);
        else
            validateMinAndMaxSegmentCount(sourceMessage, SegmentName.PID, 2, 2);

        if (!mergeMessageTypes.contains(messageType))
            validateExactlyOneSegment(sourceMessage, SegmentName.PD1);
        else
            validateNoSegments(sourceMessage, SegmentName.PD1);

        if (!swapMessageTypes.contains(messageType)) {
            validateZeroOrOneSegments(sourceMessage, SegmentName.PV1);

            long segmentCount = sourceMessage.getSegmentCount(SegmentName.PV1);

            validateMinAndMaxSegmentCount(sourceMessage, SegmentName.PV2, 0, segmentCount);
        }
    }
}
