package org.endeavourhealth.hl7transform.transforms.barts;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.SegmentName;
import org.endeavourhealth.hl7transform.Transform;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.endeavourhealth.hl7transform.transforms.barts.pretransform.BartsPreTransform;
import org.endeavourhealth.hl7transform.transforms.barts.transforms.*;
import org.hl7.fhir.instance.model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BartsAdtTransform extends Transform {

    private HashMap<String, Class<? extends Segment>> zSegments = new HashMap<>();

    public BartsAdtTransform() {
    }

    public List<String> getSupportedSendingFacilities() {
        return Arrays.asList(new String[] { BartsConstants.sendingFacility });
    }

    public HashMap<String, Class<? extends Segment>> getZSegments() {
        return zSegments;
    }

    public AdtMessage preTransform(AdtMessage sourceMessage) throws Exception {
        Validate.notNull(sourceMessage);
        validateSendingFacility(sourceMessage);

        return BartsPreTransform.preTransform(sourceMessage);
    }

    public Bundle transform(AdtMessage sourceMessage, Mapper mapper) throws Exception {
        Validate.notNull(sourceMessage);
        validateSendingFacility(sourceMessage);
        validateSegmentCounts(sourceMessage);

        ResourceContainer targetResources = new ResourceContainer();

        ///////////////////////////////////////////////////////////////////////////
        // create main hospital organisation
        //
        BartsOrganizationTransform bartsOrganizationTransform = new BartsOrganizationTransform(mapper, targetResources);
        Organization mainHospitalOrganisation = bartsOrganizationTransform.createBartsManagingOrganisation(sourceMessage);
        targetResources.addResource(mainHospitalOrganisation, ResourceTag.MainHospitalOrganisation);

        ///////////////////////////////////////////////////////////////////////////
        // create usual gp organisation
        //
        Organization mainGPOrganisation = bartsOrganizationTransform.createMainPrimaryCareProviderOrganisation(sourceMessage);

        if (mainGPOrganisation != null)
            targetResources.addResource(mainGPOrganisation, ResourceTag.MainPrimaryCareProviderOrganisation);

        ///////////////////////////////////////////////////////////////////////////
        // create usual gp practitioner
        //
        BartsPractitionerTransform bartsPractitionerTransform = new BartsPractitionerTransform(mapper, targetResources);
        Practitioner mainGPPractitioner = bartsPractitionerTransform.createMainPrimaryCareProviderPractitioner(sourceMessage);

        if (mainGPPractitioner != null)
            targetResources.addResource(mainGPPractitioner, ResourceTag.MainPrimaryCareProviderPractitioner);

        ///////////////////////////////////////////////////////////////////////////
        // create patient
        //
        BartsPatientTransform bartsPatientTransform = new BartsPatientTransform(mapper, targetResources);
        Patient patient = bartsPatientTransform.transform(sourceMessage);
        targetResources.addResource(patient, ResourceTag.PatientSubject);

        ///////////////////////////////////////////////////////////////////////////
        // create episode of care
        //
        // and any associated organisations (/services), practitioners, locations
        //
        BartsEpisodeOfCareTransform bartsEpisodeOfCareTransform = new BartsEpisodeOfCareTransform(mapper, targetResources);
        EpisodeOfCare episodeOfCare = bartsEpisodeOfCareTransform.transform(sourceMessage);

        if (episodeOfCare != null)
            targetResources.addResource(episodeOfCare);

        ///////////////////////////////////////////////////////////////////////////
        // create encounter
        //
        BartsEncounterTransform bartsEncounterTransform = new BartsEncounterTransform(mapper, targetResources);
        Encounter encounter = bartsEncounterTransform.transform(sourceMessage);

        if (encounter != null)
            targetResources.addResource(encounter);

        ///////////////////////////////////////////////////////////////////////////
        // create merge information
        //
        BartsMergeTransform bartsMergeTransform = new BartsMergeTransform(mapper, targetResources);
        Parameters parameters = bartsMergeTransform.transform(sourceMessage);

        if (parameters != null)
            targetResources.addResource(parameters);

        ///////////////////////////////////////////////////////////////////////////
        // create message header
        //
        BartsMessageHeaderTransform bartsMessageHeaderTransform = new BartsMessageHeaderTransform(mapper, targetResources);
        MessageHeader messageHeader = bartsMessageHeaderTransform.transform(sourceMessage);
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

        validateExactlyOneSegment(sourceMessage, SegmentName.MSH);
        validateExactlyOneSegment(sourceMessage, SegmentName.EVN);

        if (!swapMessageTypes.contains(messageType))
            validateExactlyOneSegment(sourceMessage, SegmentName.PID);
        else
            validateMinAndMaxSegmentCount(sourceMessage, SegmentName.PID, 2, 2);

        if (!swapMessageTypes.contains(messageType)) {
            validateZeroOrOneSegments(sourceMessage, SegmentName.PV1);

            long segmentCount = sourceMessage.getSegmentCount(SegmentName.PV1);

            validateMinAndMaxSegmentCount(sourceMessage, SegmentName.PV2, 0, segmentCount);
        }

        if (BartsMergeTransform.MergeMessageTypes.contains(messageType))
            validateExactlyOneSegment(sourceMessage, SegmentName.MRG);
    }
}
