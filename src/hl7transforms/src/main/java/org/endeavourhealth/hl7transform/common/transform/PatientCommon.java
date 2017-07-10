package org.endeavourhealth.hl7transform.common.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.datatypes.XpnInterface;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.PidSegment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.common.converters.NameConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;

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

    public static List<Identifier> convertPatientIdentifiers(List<Cx> cxs, Mapper mapper, boolean allowMultipleWithSameSystem) throws TransformException, MapperException {
        List<Identifier> targetIdentifiers = new ArrayList<>();

        for (Cx cx : cxs) {
            Identifier identifier = IdentifierConverter.createIdentifier(cx, ResourceType.Patient, mapper);

            if (identifier == null)
                continue;

            if (allowMultipleWithSameSystem) {
                if (targetIdentifiers
                        .stream()
                        .anyMatch(t -> StringUtils.equals(identifier.getSystem(), t.getSystem()) && StringUtils.equals(identifier.getValue(), t.getValue()))) {

                    continue;
                }
            } else {
                if (targetIdentifiers
                        .stream()
                        .anyMatch(t -> StringUtils.equals(identifier.getSystem(), t.getSystem()))) {

                    continue;
                }
            }

            targetIdentifiers.add(identifier);
        }

        return targetIdentifiers;
    }

    public static String getPatientIdentifierValueByTypeCode(List<Cx> cxs, String patientIdentifierTypeCode) {
        return cxs
                .stream()
                .filter(t -> t != null)
                .filter(t -> patientIdentifierTypeCode.equals(t.getIdentifierTypeCode()))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());
    }

    public static String getPatientIdentifierValueByTypeCode(AdtMessage message, String patientIdentifierTypeCode) {
        return getPatientIdentifierValueByTypeCode(PatientCommon.getAllPatientIdentifiers(message), patientIdentifierTypeCode);
    }

    public static String getPatientIdentifierValueByAssigningAuth(List<Cx> cxs, String patientIdentifierAssigningAuthority) {
        return cxs
                .stream()
                .filter(t -> t != null)
                .filter(t -> patientIdentifierAssigningAuthority.equals(t.getAssigningAuthority()))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());
    }

    public static String getPatientIdentifierValueByAssigningAuth(AdtMessage message, String patientIdentifierAssigningAuthority) {
        return getPatientIdentifierValueByAssigningAuth(PatientCommon.getAllPatientIdentifiers(message), patientIdentifierAssigningAuthority);
    }
}
