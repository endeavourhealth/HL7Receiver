package org.endeavourhealth.hl7transform.profiles;

import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.Segment;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.transform.TransformException;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Patient;

import java.util.HashMap;

public interface TransformProfile {
    HashMap<String, Class<? extends Segment>> getZSegments();
    AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException;

    String getUniqueMessageHeaderString(AdtMessage source) throws TransformException;
    String getUniquePatientString(AdtMessage message) throws TransformException;
    String getUniqueEncounterString(AdtMessage message) throws TransformException;
    void postTransformPatient(AdtMessage message, Patient patient) throws TransformException, ParseException;
    void postTransformEncounter(AdtMessage message, Encounter encounter) throws TransformException, ParseException;
}
