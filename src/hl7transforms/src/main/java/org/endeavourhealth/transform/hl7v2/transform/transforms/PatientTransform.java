package org.endeavourhealth.transform.hl7v2.transform.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.hl7v2.mapper.Mapper;
import org.endeavourhealth.transform.hl7v2.mapper.MapperException;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.*;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Nk1Segment;
import org.endeavourhealth.hl7parser.segments.PidSegment;
import org.endeavourhealth.transform.hl7v2.profiles.TransformProfile;
import org.endeavourhealth.transform.hl7v2.transform.ResourceContainer;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.endeavourhealth.transform.hl7v2.transform.converters.*;
import org.endeavourhealth.transform.hl7v2.transform.converters.ExtensionHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PatientTransform {

    public static void fromHl7v2(AdtMessage source, TransformProfile transformProfile, Mapper mapper, ResourceContainer targetResources) throws Exception {
        Validate.notNull(source);
        Validate.notNull(transformProfile);
        Validate.notNull(mapper);

        Patient target = new Patient();

        setId(source, transformProfile, target, mapper);

        addNames(source.getPidSegment(), target);
        setBirthAndDeath(source.getPidSegment(), target);
        setSex(source.getPidSegment(), target);
        addIdentifiers(source, target);
        setAddress(source.getPidSegment(), target);
        setContactPoint(source.getPidSegment(), target);
        setCommunication(source.getPidSegment(), target);
        setCodedElements(source.getPidSegment(), target);
//        setPrimaryCareProvider(source.getPd1Segment(), target);
        addPatientContacts(source, target);

        // managing organization

        // site specific transformations
        transformProfile.postTransformPatient(source, target);

        targetResources.add(target);
    }

    private static void setId(AdtMessage source, TransformProfile transformProfile, Patient target, Mapper mapper) throws MapperException, TransformException {
        String uniqueIdentifyingString = transformProfile.getUniquePatientString(source);
        UUID resourceUuid = mapper.mapResourceUuid(ResourceType.Patient, uniqueIdentifyingString);

        target.setId(resourceUuid.toString());
    }

    private static void addNames(PidSegment sourcePid, Patient target) throws TransformException {
        for (HumanName name : NameConverter.convert(sourcePid.getPatientNames()))
            target.addName(name);

        for (HumanName name : NameConverter.convert(sourcePid.getPatientAlias()))
            target.addName(name);
    }

    public static List<Cx> getAllPatientIdentifiers(AdtMessage source) {
        List<Cx> patientIdentifiers = new ArrayList<>();

        patientIdentifiers.addAll(source.getPidSegment().getInternalPatientId());
        patientIdentifiers.addAll(source.getPidSegment().getAlternatePatientId());
        patientIdentifiers.add(source.getPidSegment().getExternalPatientId());

        return patientIdentifiers;
    }

    private static void addIdentifiers(AdtMessage source, Patient target) {
        List<Cx> identifiers = getAllPatientIdentifiers(source);

        for (Cx cx : identifiers) {
            Identifier identifier = createPatientIdentifier(cx, source.getMshSegment().getSendingFacility());

            if (identifier != null)
                target.addIdentifier(identifier);
        }
    }

    public static Identifier createPatientIdentifier(CxInterface source, String sendingFacility) {
        if (source == null)
            return null;

        if (StringUtils.isBlank(source.getId()))
            return null;

        String identifierSystem = getPatientIdentifierSystem(source, sendingFacility);

        Identifier identifier = new Identifier();

        if (StringUtils.isNotBlank(identifierSystem))
            identifier.setSystem(identifierSystem);

        identifier.setValue(StringUtils.deleteWhitespace(source.getId()));

        return identifier;
    }

    private static String getPatientIdentifierSystem(CxInterface source, String sendingFacility) {
        String identifierTypeCode = source.getIdentifierTypeCode();
        String assigningAuthority = source.getAssigningAuthority();

        if (StringUtils.isBlank(identifierTypeCode) && StringUtils.isBlank(assigningAuthority))
            return null;

        if (identifierTypeCode.equals("NHS"))
            return FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER;

        return FhirUri.getHl7v2LocalPatientIdentifierSystem(sendingFacility, assigningAuthority, identifierTypeCode);
    }

//    private static void setPrimaryCareProvider(Pd1Segment sourcePd1, Patient target) {
//        if (sourcePd1 != null) {
//            if (sourcePd1.getPatientPrimaryCareProvider() != null) {
//                for (Xcn xcn : sourcePd1.getPatientPrimaryCareProvider()){
//                    Reference reference = new Reference();
//                    reference.setReference(xcn.getId());
//                    reference.setDisplay(NameConverter.getNameAsString(xcn));
//                    target.addCareProvider(reference);
//                }
//            }
//        }
//    }

    private static void setCodedElements(PidSegment sourcePid, Patient target) throws TransformException {
        if (sourcePid.getReligion() != null)
            target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.PATIENT_RELIGION, sourcePid.getReligion().getAsString()));

        if (sourcePid.getEthnicGroups() != null)
            for (Ce ce : sourcePid.getEthnicGroups())
                target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.PATIENT_ETHNICITY, ce.getAsString()));

        if (sourcePid.getTraceStatus() != null)
            target.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.PATIENT_NHS_NUMBER_VERIFICATION_STATUS, sourcePid.getTraceStatus().getAsString()));


        if (sourcePid.getMaritalStatus() != null)
            target.setMaritalStatus(getCodeableConcept(sourcePid.getMaritalStatus()));
    }

    private static void setAddress(PidSegment sourcePid, Patient target) throws TransformException {
        for (Address address : AddressConverter.convert(sourcePid.getAddresses()))
            target.addAddress(address);
    }

    private static void setSex(PidSegment sourcePid, Patient target) throws TransformException {
        if (!StringUtils.isEmpty(sourcePid.getSex()))
            target.setGender(getSex(sourcePid.getSex()));
    }

    private static void setContactPoint(PidSegment sourcePid, Patient target) throws TransformException {
        for (ContactPoint cp : TelecomConverter.convert(sourcePid.getHomeTelephones()))
            target.addTelecom(cp);

        for (ContactPoint cp : TelecomConverter.convert(sourcePid.getBusinessTelephones()))
            target.addTelecom(cp);
    }

    private static Enumerations.AdministrativeGender getSex(String gender) throws TransformException {
        return SexConverter.convert(gender);
    }

    private static void setBirthAndDeath(PidSegment sourcePid, Patient target) throws ParseException, TransformException {
        if (sourcePid.getDateOfBirth() != null) {
            target.setBirthDate(sourcePid.getDateOfBirth().asDate());
            //ADD EXTENSION FOR TIME OF BIRTH
            if (sourcePid.getDateOfBirth().hasTimeComponent()) {
                target.addExtension(ExtensionHelper.createDateTimeTypeExtension(FhirExtensionUri.PATIENT_BIRTH_DATE_TIME, DateConverter.getDateType(sourcePid.getDateOfBirth())));
            }
        }

        if (sourcePid.getDateOfDeath() != null) {
            target.setDeceased(DateConverter.getDateType(sourcePid.getDateOfDeath()));
        } else if (isDeceased(sourcePid.getDeathIndicator()))
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

    private static void addPatientContacts(AdtMessage source, Patient target) throws TransformException, ParseException {
        for (Nk1Segment nk1 : source.getNk1Segments())
            addPatientContact(nk1, target);
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
            contactComponent.addExtension(ExtensionHelper.createDateTimeTypeExtension(FhirExtensionUri.PATIENT_CONTACT_DOB,
                    DateConverter.getDateType(sourceNk1.getDateTimeOfBirth())));

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
