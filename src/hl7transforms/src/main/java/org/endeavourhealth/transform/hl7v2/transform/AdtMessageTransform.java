package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.mapper.CodeMapper;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.parser.segments.Dg1Segment;
import org.endeavourhealth.transform.hl7v2.parser.segments.ObxSegment;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.AdditionalPatientInformation;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.AdditionalVisitTransform;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.QuestionnaireTransform;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class AdtMessageTransform {
    public static Bundle transform(AdtMessage sourceMessage, CodeMapper codeMapper) throws Exception {
        Validate.notNull(sourceMessage);
        String sendingFacility;

        List<Resource> targetResources = new ArrayList<>();

        targetResources.add(MessageHeaderTransform.fromHl7v2(sourceMessage.getMshSegment()));

        sendingFacility = sourceMessage.getMshSegment().getSendingFacility();

        Patient patient = PatientTransform.fromHl7v2(sourceMessage);

        if (sourceMessage.hasZpiSegment())
            patient = AdditionalPatientInformation.addAdditionalInformation(patient, sourceMessage.getZpiSegment());

        targetResources.add(patient);

        if (sourceMessage.hasPv1Segment()) {
            Encounter encounter = PatientVisitTransform.fromHl7v2(sourceMessage.getPv1Segment(), sendingFacility);

            if (sourceMessage.hasPv2Segment()){
                encounter = PatientVisitTransform.addAdditionalInformation(encounter, sourceMessage.getPv2Segment());
            }

            if (sourceMessage.hasZviSegment()){
                encounter = AdditionalVisitTransform.addAdditionalInformation(encounter, sourceMessage.getZviSegment());
            }

            targetResources.add(encounter);
        }

        List<ObxSegment> sourceObxSegments = sourceMessage.getObxSegments();

        for (ObxSegment obx : sourceObxSegments) {
            targetResources.add(ObservationTransform.fromHl7v2(obx));
        }

        List<Dg1Segment> sourceDg1Segments = sourceMessage.getDg1Segments();

        for (Dg1Segment dg1 : sourceDg1Segments) {
            targetResources.add(DiagnosisTransform.fromHl7v2(dg1));
        }

        for (Practitioner practitioner : PractitionerTransform.fromHl7v2(sourceMessage))
            targetResources.add(practitioner);

        for (Location location : LocationTransform.fromHl7v2(sourceMessage))
            targetResources.add(location);

        if (sourceMessage.hasZqaSegment()) {
            targetResources.add(QuestionnaireTransform.fromHl7v2(sourceMessage.getZqaSegment()));
        }


        if (sourceMessage.hasAccSegment()){

        }




        return createBundle(targetResources);
    }

    private static Bundle createBundle(List<Resource> resources) {
        Bundle bundle = new Bundle()
                .setType(Bundle.BundleType.MESSAGE);

        for (Resource resource : resources)
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(resource));

        return bundle;
    }
}
