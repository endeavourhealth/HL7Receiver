package org.endeavourhealth.transform.hl7v2.profiles.homerton;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.transform.hl7v2.profiles.TransformProfile;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.*;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.transforms.AdditionalPatientInfoTransform;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.transforms.AdditionalVisitTransform;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.pretransform.HomertonPreTransform;
import org.endeavourhealth.transform.hl7v2.transform.transforms.PatientTransform;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.HashMap;

public class HomertonTransformProfile implements TransformProfile {

    private HashMap<String, Class<? extends Segment>> zSegments = new HashMap<>();
    private String patientIdentifierTypeCode = "CNN";
    private String encounterAssigningAuthority = "Homerton FIN";
    private String uniqueIdentifierPrefix = "HOMERTON";

    public HomertonTransformProfile() {
        zSegments.put("ZAL", ZalSegment.class);
        zSegments.put("ZPI", ZpiSegment.class);
        zSegments.put("ZQA", ZqaSegment.class);
        zSegments.put("ZVI", ZviSegment.class);
    }

    @Override
    public HashMap<String, Class<? extends Segment>> getZSegments() {
        return zSegments;
    }

    @Override
    public AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException {
        return HomertonPreTransform.preTransform(sourceMessage);
    }

    @Override
    public String getUniqueMessageHeaderString(AdtMessage source) throws TransformException {

        if (StringUtils.isBlank(source.getMshSegment().getMessageControlId()))
            throw new TransformException("Cannot create unique resource identifying string as MessageControlId is blank");

        return StringUtils.deleteWhitespace(uniqueIdentifierPrefix + "-MessageHeader-" + source.getMshSegment().getMessageControlId());
    }

    @Override
    public String getUniquePatientString(AdtMessage message) throws TransformException {
        Cx cx = PatientTransform.getAllPatientIdentifiers(message)
                .stream()
                .filter(t -> patientIdentifierTypeCode.equals(t.getIdentifierTypeCode()))
                .collect(StreamExtension.firstOrNullCollector());

        if (cx == null)
            throw new TransformException("Could not find patient identifier with type of " + patientIdentifierTypeCode);

        if (StringUtils.isBlank(cx.getId()))
            throw new TransformException("Patient identifier with type of " + patientIdentifierTypeCode + " has empty value");

        return StringUtils.deleteWhitespace(uniqueIdentifierPrefix + "-Patient-" + patientIdentifierTypeCode + "-" + cx.getId());
    }

    @Override
    public String getUniqueEncounterString(AdtMessage message) throws TransformException {
        Cx cx = message.getPidSegment().getPatientAccountNumber();

        if (cx == null)
            throw new TransformException("Encounter number (PID.18) is null");

        if (StringUtils.isEmpty(cx.getAssigningAuthority()))
            throw new TransformException("Encounter number (PID.18) assigning authority is empty");

        if (StringUtils.isEmpty(cx.getId()))
            throw new TransformException("Encounter number (PID.18) id is empty");

        if (!encounterAssigningAuthority.equals(cx.getAssigningAuthority()))
            throw new TransformException("Encounter number assigning authority does not match");

        return StringUtils.deleteWhitespace(getUniquePatientString(message) + "-Encounter-" + cx.getAssigningAuthority() + "-" + cx.getId());
    }

    @Override
    public void postTransformPatient(AdtMessage message, Patient target) throws TransformException, ParseException {
        if (message.hasSegment(HomertonSegmentName.ZPI))
            AdditionalPatientInfoTransform.addAdditionalInformation(target, message.getSegment(HomertonSegmentName.ZPI, ZpiSegment.class));
    }

    @Override
    public void postTransformEncounter(AdtMessage message, Encounter target) throws TransformException, ParseException {
        if (message.hasSegment(HomertonSegmentName.ZVI))
            AdditionalVisitTransform.addAdditionalInformation(target, message.getSegment(HomertonSegmentName.ZVI, ZviSegment.class));
    }
}
