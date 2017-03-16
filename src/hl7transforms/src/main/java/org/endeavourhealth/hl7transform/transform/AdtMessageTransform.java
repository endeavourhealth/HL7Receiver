package org.endeavourhealth.hl7transform.transform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.profiles.TransformProfile;
import org.endeavourhealth.hl7transform.transform.transforms.EncounterTransform;
import org.endeavourhealth.hl7transform.transform.transforms.MessageHeaderTransform;
import org.endeavourhealth.hl7transform.transform.transforms.PatientTransform;
import org.hl7.fhir.instance.model.*;

public class AdtMessageTransform {
    public static Bundle transform(AdtMessage sourceMessage, TransformProfile transformProfile, Mapper mapper) throws Exception {
        Validate.notNull(sourceMessage);

        ResourceContainer targetResources = new ResourceContainer();

        MessageHeaderTransform.fromHl7v2(sourceMessage, transformProfile, mapper, targetResources);
        PatientTransform.fromHl7v2(sourceMessage, transformProfile, mapper, targetResources);
        EncounterTransform.fromHl7v2(sourceMessage, transformProfile, mapper, targetResources);

//
//        for (ObxSegment obx : sourceMessage.getObxSegments())
//            targetResources.add(ObservationTransform.fromHl7v2(obx));
//
//        for (Dg1Segment dg1 : sourceMessage.getDg1Segments())
//            targetResources.add(DiagnosisTransform.fromHl7v2(dg1));
//
//        if (sourceMessage.hasZqaSegment())
//            targetResources.add(QuestionnaireTransform.fromHl7v2(sourceMessage.getZqaSegment()));

        return targetResources.createBundle();
    }
}
