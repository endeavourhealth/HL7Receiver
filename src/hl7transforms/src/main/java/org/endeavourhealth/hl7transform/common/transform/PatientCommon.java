package org.endeavourhealth.hl7transform.common.transform;

import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.datatypes.XpnInterface;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.PidSegment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.NameConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientCommon {

    public static void addNames(AdtMessage source, Patient target, Mapper mapper) throws TransformException, MapperException {
        List<HumanName> names = NameConverter.convert(PatientCommon.getPatientNames(source.getPidSegment()), mapper);

        for (HumanName name : names)
            if (name != null)
                target.addName(name);
    }

    public static List<XpnInterface> getPatientNames(PidSegment pidSegment) {
        List<XpnInterface> names = new ArrayList<>();

        if (pidSegment.getPatientNames() != null)
            names.addAll(pidSegment.getPatientNames());

        if (pidSegment.getPatientAlias() != null)
            names.addAll(pidSegment.getPatientAlias());

        return names;
    }

    public static List<Cx> getAllPatientIdentifiers(AdtMessage source) {
        List<Cx> patientIdentifiers = new ArrayList<>();

        if (source.getPidSegment().getExternalPatientId() != null)
            patientIdentifiers.add(source.getPidSegment().getExternalPatientId());

        if (source.getPidSegment().getInternalPatientId() != null)
            patientIdentifiers.addAll(source.getPidSegment().getInternalPatientId());

        return patientIdentifiers;
    }
}
