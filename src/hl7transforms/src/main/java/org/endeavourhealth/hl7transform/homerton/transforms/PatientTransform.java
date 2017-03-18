package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.Field;
import org.endeavourhealth.hl7parser.Helpers;
import org.endeavourhealth.hl7parser.segments.Pd1Segment;
import org.endeavourhealth.hl7transform.common.converters.ExtensionHelper;
import org.endeavourhealth.hl7transform.homerton.parser.zsegments.HomertonSegmentName;
import org.endeavourhealth.hl7transform.homerton.parser.zsegments.ZpiSegment;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.*;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.*;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Nk1Segment;
import org.endeavourhealth.hl7parser.segments.PidSegment;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PatientTransform {

    private static final String patientIdentifierTypeCode = "CNN";

    private Mapper mapper;
    private ResourceContainer targetResources;

    public PatientTransform(Mapper mapper, ResourceContainer targetResources) {
        this.mapper = mapper;
        this.targetResources = targetResources;
    }

    public void transform(AdtMessage source) throws Exception {
        Validate.notNull(source);

        Patient target = new Patient();

        setId(source, target);
        addNames(source.getPidSegment(), target);
        setDateOfBirth(source.getPidSegment(), target);
        setDateOfDeath(source.getPidSegment(), target);
        setSex(source.getPidSegment(), target);
        addIdentifiers(source, target);
        setAddress(source, target);
        setContactPoint(source.getPidSegment(), target);
        setCommunication(source.getPidSegment(), target);
        addEthnicity(source.getPidSegment(), target);
        addReligion(source.getPidSegment(), target);
        addMaritalStatus(source.getPidSegment(), target);
        setPrimaryCareProvider(source, target);
        addPatientContacts(source, target);
        setManagingOrganization(source, target);

        targetResources.addResource(target);
    }

    private void setId(AdtMessage source, Patient target) throws MapperException, TransformException {
        String uniqueIdentifyingString = getUniquePatientString(source);
        UUID resourceUuid = mapper.mapResourceUuid(ResourceType.Patient, uniqueIdentifyingString);

        target.setId(resourceUuid.toString());
    }

    public static String getUniquePatientString(AdtMessage message) throws TransformException {
        Cx cx = PatientTransform.getAllPatientIdentifiers(message)
                .stream()
                .filter(t -> patientIdentifierTypeCode.equals(t.getIdentifierTypeCode()))
                .collect(StreamExtension.firstOrNullCollector());

        if (cx == null)
            throw new TransformException("Could not find patient identifier with type of " + patientIdentifierTypeCode);

        if (StringUtils.isBlank(cx.getId()))
            throw new TransformException("Patient identifier with type of " + patientIdentifierTypeCode + " has empty value");

        return StringUtils.deleteWhitespace("Patient-" + patientIdentifierTypeCode + "-" + cx.getId());
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

            if (identifier != null) {

                if (identifier.getSystem() != null)
                    if (identifier.getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER))
                        addTraceStatus(source.getPidSegment(), identifier);

                target.addIdentifier(identifier);
            }
        }
    }

    private static void addTraceStatus(PidSegment sourcePid, Identifier target) {
        if (sourcePid.getTraceStatus() == null)
            return;

        if (StringUtils.isBlank(sourcePid.getTraceStatus().getAsString()))
            return;

        target.addExtension(new Extension()
                .setUrl(FhirExtensionUri.PATIENT_NHS_NUMBER_VERIFICATION_STATUS)
                .setValue(new CodeableConcept()
                        .setText(sourcePid.getTraceStatus().getAsString())));
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

    private void setPrimaryCareProvider(AdtMessage source, Patient target) throws MapperException {

        Pd1Segment pd1Segment = source.getPd1Segment();

        if (pd1Segment == null)
            return;

        /*
            Homerton specific

            PD1.4 incorrectly contains both primary care organisation and doctor
            It should only contain the doctor.

            Homerton's PD1.4:

            1          2       3         5           6       7            8        9.1      9.2      9.3          14.1       14.2
            DoctorCode^Surname^Forename^^PhoneNumber^OdsCode^PracticeName^Address1^Address2&Address3&Address4^^^^^PctOdsCode&ShaOdsCode

            Examples:

            G3339325^SMITH^A^^1937573848^B86010^DR SR LIGHTFOOT & PARTNERS^Church View Surgery^School Lane&&LS22 5BQ^^^^^Q12&5HJ
            G3426500^LYLE^ROBERT^^020 89867111^F84003^LOWER CLAPTON GROUP PRACTICE^Lower Clapton Health Ctr.^36 Lower Clapton Road&London&E5 0PD^^^^^Q06&5C3
         */

        Field field = pd1Segment.getField(4);

        String phoneNumber;
        String odsCode;
        String practiceName;
        List<String> addressLines = new ArrayList<>();
        String city = null;
        String postcode = null;

        phoneNumber = field.getComponentAsString(5);
        odsCode = field.getComponentAsString(6);
        practiceName = field.getComponentAsString(7);
        addressLines.add(field.getComponentAsString(8));

        List<String> components = Helpers.split(field.getComponentAsString(9), "&");

        if (components.size() > 0)
            addressLines.add(components.get(0));

        if (components.size() > 1)
            city = components.get(1);

        if (components.size() > 2)
            postcode = components.get(2);

        OrganizationTransform organizationTransform = new OrganizationTransform(mapper, targetResources);
        Reference organisationReference = organizationTransform.createGeneralPracticeOrganisation(odsCode, practiceName, addressLines, city, postcode, phoneNumber);

        String gmcCode = field.getComponentAsString(1);
        String surname = field.getComponentAsString(2);
        String forenames = field.getComponentAsString(3);

        PractitionerTransform practitionerTransform = new PractitionerTransform(source.getMshSegment().getSendingFacility(), mapper, targetResources);
        Reference practitionerReference = practitionerTransform.createPrimaryCarePractitioner(gmcCode, surname, forenames, organisationReference);

        if (organisationReference != null)
            target.addCareProvider(organisationReference);

        if (practitionerReference != null)
            target.addCareProvider(practitionerReference);
    }

    private static void addReligion(PidSegment sourcePid, Patient target) {
        if (sourcePid.getReligion() == null)
            return;

        if (StringUtils.isBlank(sourcePid.getReligion().getAsString()))
            return;

        target.addExtension(new Extension()
                .setUrl(FhirExtensionUri.PATIENT_RELIGION)
                .setValue(new CodeableConcept()
                        .setText(sourcePid.getReligion().getAsString())));
    }

    private static void addEthnicity(PidSegment sourcePid, Patient target) throws TransformException {
        if (sourcePid.getEthnicGroups() == null)
            return;

        for (Ce ce : sourcePid.getEthnicGroups()) {
            if (StringUtils.isBlank(ce.getAsString()))
                continue;

            target.addExtension(new Extension()
                    .setUrl(FhirExtensionUri.PATIENT_ETHNICITY)
                    .setValue(new CodeableConcept()
                            .setText(ce.getAsString())));
        }
    }

    private static void addMaritalStatus(PidSegment sourcePid, Patient target) {
        if (sourcePid.getMaritalStatus() == null)
            return;

        if (StringUtils.isEmpty(sourcePid.getMaritalStatus().getAsString()))
            return;

        target.setMaritalStatus(new CodeableConcept()
                .setText(sourcePid.getMaritalStatus().getAsString()));
    }

    private static void setAddress(AdtMessage source, Patient target) throws TransformException {

        PidSegment pidSegment = source.getPidSegment();

        for (Address address : AddressConverter.convert(pidSegment.getAddresses()))
            if (address != null)
                target.addAddress(address);

        ZpiSegment zpiSegment = source.getSegment(HomertonSegmentName.ZPI, ZpiSegment.class);

        if (zpiSegment != null)
            if (zpiSegment.getPatientTemporaryAddress() != null)
                for (Address address : AddressConverter.convert(zpiSegment.getPatientTemporaryAddress()))
                    if (address != null)
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

    private static void setDateOfBirth(PidSegment sourcePid, Patient target) throws ParseException, TransformException {
        if (sourcePid.getDateOfBirth() == null)
            return;

        target.setBirthDate(sourcePid.getDateOfBirth().asDate());

        if (sourcePid.getDateOfBirth().hasTimeComponent())
            target.addExtension(ExtensionHelper.createDateTimeTypeExtension(FhirExtensionUri.PATIENT_BIRTH_DATE_TIME, DateConverter.getDateType(sourcePid.getDateOfBirth())));
    }

    private static void setDateOfDeath(PidSegment sourcePid, Patient target) throws ParseException, TransformException {
        if (sourcePid.getDateOfDeath() != null)
            target.setDeceased(DateConverter.getDateType(sourcePid.getDateOfDeath()));
        else if (isDeceased(sourcePid.getDeathIndicator()))
            target.setDeceased(new BooleanType(true));
    }

    private static void setCommunication(PidSegment sourcePid, Patient target) throws ParseException, TransformException {
        if (sourcePid.getPrimaryLanguage() != null) {
            Patient.PatientCommunicationComponent communicationComponent = new Patient.PatientCommunicationComponent();
            communicationComponent.setLanguage(new CodeableConcept().setText(sourcePid.getPrimaryLanguage().getAsString()));
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
            contactComponent.addRelationship(new CodeableConcept().setText(sourceNk1.getRelationship().getAsString()));

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
            contactComponent.addRelationship(new CodeableConcept().setText((sourceNk1.getContactRole().getAsString())));

        if (sourceNk1.getDateTimeOfBirth() != null)
            contactComponent.addExtension(ExtensionHelper.createDateTimeTypeExtension(FhirExtensionUri.PATIENT_CONTACT_DOB,
                    DateConverter.getDateType(sourceNk1.getDateTimeOfBirth())));

        if (sourceNk1.getPrimaryLanguage() != null)
            contactComponent.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.PATIENT_CONTACT_MAIN_LANGUAGE, sourceNk1.getPrimaryLanguage().getAsString()));

        target.addContact(contactComponent);
    }

    private void setManagingOrganization(AdtMessage source, Patient target) throws MapperException, TransformException {
        target.setManagingOrganization(this.targetResources.getManagingOrganisation());
    }
}
