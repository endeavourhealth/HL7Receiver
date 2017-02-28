package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.*;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.parser.segments.MshSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.Nk1Segment;
import org.endeavourhealth.transform.hl7v2.parser.segments.PidSegment;
import org.endeavourhealth.transform.hl7v2.transform.converters.*;
import org.endeavourhealth.transform.hl7v2.transform.converters.ExtensionHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class PatientTransform {

    public static Patient fromHl7v2(AdtMessage source) throws ParseException, TransformException {
        MshSegment sourceMsh = source.getMshSegment();
        PidSegment sourcePid = source.getPidSegment();
        List<Nk1Segment> sourceNk1Segments = source.getNk1Segments();

        Patient target = new Patient();

        addIdentifiers(sourcePid, sourceMsh, target);

        addNames(sourcePid, target);

        setBirthAndDeath(sourcePid, target);

        setCommunication(sourcePid, target);

        if (sourcePid.getReligion() != null)
            target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.PATIENT_RELIGION, sourcePid.getReligion().getAsString()));

        if (sourcePid.getEthnicGroups() != null)
            for (Ce ce : sourcePid.getEthnicGroups())
                target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.PATIENT_ETHNICITY, ce.getAsString()));

        if (sourcePid.getTraceStatus() != null)
            target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.PATIENT_NHS_NUMBER_VERIFICATION_STATUS, sourcePid.getTraceStatus().getAsString()));

        if (!StringUtils.isEmpty(sourcePid.getSex()))
            target.setGender(getSex(sourcePid.getSex()));

        for (ContactPoint cp : TelecomConverter.convert(sourcePid.getHomeTelephones()))
            target.addTelecom(cp);

        for (ContactPoint cp : TelecomConverter.convert(sourcePid.getBusinessTelephones()))
            target.addTelecom(cp);

        for (Address address : AddressConverter.convert(sourcePid.getAddresses()))
            target.addAddress(address);

        if (sourcePid.getMaritalStatus() != null)
            target.setMaritalStatus(getCodeableConcept(sourcePid.getMaritalStatus()));


        for (Nk1Segment nk1 : sourceNk1Segments)
            addPatientContact(nk1, target);

        return target;
    }

    private static Enumerations.AdministrativeGender getSex(String gender) throws TransformException {
        return SexConverter.convert(gender);
    }

    private static void setBirthAndDeath(PidSegment sourcePid, Patient target) throws ParseException, TransformException {
        if (sourcePid.getDateOfBirth() != null)
            target.setBirthDate(DateHelper.fromLocalDateTime(sourcePid.getDateOfBirth()));

        if (sourcePid.getDateOfDeath() != null)
            target.setDeceased(new DateTimeType(DateHelper.fromLocalDateTime(sourcePid.getDateOfDeath())));
        else if (isDeceased(sourcePid.getDeathIndicator()))
            target.setDeceased(new BooleanType(true));
    }

    private static void setCommunication(PidSegment sourcePid, Patient target) throws ParseException, TransformException {
        if (sourcePid.getPrimaryLanguage() != null) {
            Patient.PatientCommunicationComponent communicationComponent = new Patient.PatientCommunicationComponent();
            communicationComponent.setLanguage(getCodeableConcept(sourcePid.getPrimaryLanguage()));
            communicationComponent.setPreferred(true);
            target.addCommunication(communicationComponent);
        }
    }

    private static boolean isDeceased(String deathIndicator) throws TransformException {
        if (StringUtils.isEmpty(deathIndicator))
            return false;

        String indicator = deathIndicator.trim().toLowerCase().substring(0, 1);

        if (indicator.equals("y"))
            return true;
        else if (indicator.equals("n"))
            return false;

        throw new TransformException(indicator + " not recognised as a death indicator");
    }

    private static void addNames(PidSegment sourcePid, Patient target) throws TransformException {
        for (HumanName name : NameConverter.convert(sourcePid.getPatientNames()))
            target.addName(name);

        for (HumanName name : NameConverter.convert(sourcePid.getPatientAlias()))
            target.addName(name);
    }

    private static void addIdentifiers(PidSegment sourcePid, MshSegment sourceMsh, Patient target) {
        addIdentifier(target, sourcePid.getExternalPatientId(), sourceMsh.getSendingFacility());

        for (Cx cx : sourcePid.getInternalPatientId())
            addIdentifier(target, cx, sourceMsh.getSendingFacility());

        for (Cx cx : sourcePid.getAlternatePatientId())
            addIdentifier(target, cx, sourceMsh.getSendingFacility());
    }

    private static void addIdentifier(Patient target, Cx cx, String sendingFacility) {
        Identifier identifier = IdentifierConverter.convert(cx, sendingFacility);

        if (identifier != null)
            target.addIdentifier(identifier);

        if (identifier.getSystem().equals("http://endeavourhealth.org/fhir/v2-id-assigning-auth/HOMERTON/CNN"))
            target.setIdElement(new IdType().setValue(IdentifierHelper.generateId(identifier.getValue())));
    }

    private static void addPatientContact(Nk1Segment sourceNk1, Patient target) throws TransformException, ParseException  {
        Patient.ContactComponent contactComponent = new Patient.ContactComponent();

        for (HumanName name : NameConverter.convert(sourceNk1.getNKName()))
            contactComponent.setName(name);

        if (sourceNk1.getRelationship() != null)
            contactComponent.addRelationship(getCodeableConcept(sourceNk1.getRelationship()));

        for (ContactPoint cp : TelecomConverter.convert(sourceNk1.getPhoneNumber()))
            contactComponent.addTelecom(cp);

        for (ContactPoint cp : TelecomConverter.convert(sourceNk1.getBusinessPhoneNumber()))
            contactComponent.addTelecom(cp);

        //FHIR only allows 1 address but HL7v2 allows multiple addresses, this will currently only populate the last address.
        for (Address address : AddressConverter.convert(sourceNk1.getAddresses()))
            contactComponent.setAddress(address);

        if (!StringUtils.isEmpty(sourceNk1.getSex()))
            contactComponent.setGender(getSex(sourceNk1.getSex()));

        if (sourceNk1.getContactRole() != null)
            contactComponent.addRelationship(getCodeableConcept(sourceNk1.getContactRole()));

        if (sourceNk1.getDateTimeOfBirth() != null)
            contactComponent.addExtension(ExtensionHelper.createDateTimeExtension(FhirExtensionUri.PATIENT_CONTACT_DOB,
                    DateHelper.fromLocalDateTime(sourceNk1.getDateTimeOfBirth())));

        if (sourceNk1.getPrimaryLanguage() != null)
            contactComponent.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.PATIENT_CONTACT_MAIN_LANGUAGE, sourceNk1.getPrimaryLanguage().getAsString()));

        target.addContact(contactComponent);

    }

    private static CodeableConcept getCodeableConcept(Ce ce) throws TransformException {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setText(ce.getAsString());

        return codeableConcept;
    }

}
