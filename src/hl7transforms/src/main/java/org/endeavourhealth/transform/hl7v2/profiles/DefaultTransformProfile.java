package org.endeavourhealth.transform.hl7v2.profiles;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Patient;

import java.util.HashMap;

public class DefaultTransformProfile implements TransformProfile {

    @Override
    public HashMap<String, Class<? extends Segment>> getZSegments() {
        return new HashMap<>();
    }

    @Override
    public AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException {
        return sourceMessage;
    }

    @Override
    public String getUniqueMessageHeaderString(AdtMessage source) throws TransformException {
        return "DEFAULT-TRANSFORM-PROFILE";
    }

    @Override
    public String getUniquePatientString(AdtMessage message) {
        return "DEFAULT-TRANSFORM-PROFILE";
    }

    @Override
    public String getUniqueEncounterString(AdtMessage message) {
        return "DEFAULT-TRANSFORM-PROFILE";
    }

    @Override
    public void postTransformPatient(AdtMessage message, Patient patient) {
    }

    @Override
    public void postTransformEncounter(AdtMessage message, Encounter encounter) throws TransformException, ParseException {
    }
}
