package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.segments.Pd1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.TransformBase;
import org.endeavourhealth.hl7transform.common.converters.ExtensionHelper;
import org.endeavourhealth.hl7transform.homerton.parser.zdatatypes.Zpd;
import org.endeavourhealth.hl7transform.homerton.parser.zsegments.HomertonSegmentName;
import org.endeavourhealth.hl7transform.homerton.parser.zsegments.ZpiSegment;
import org.endeavourhealth.hl7transform.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.AddressConverter;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.NameConverter;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.TelecomConverter;
import org.endeavourhealth.hl7transform.homerton.transforms.valuesets.AdministrativeGenderVs;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
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

public class PatientTransform extends TransformBase {

    public PatientTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Patient;
    }

    public void transform(AdtMessage source) throws Exception {
        Validate.notNull(source);

        if (!source.hasPidSegment())
            throw new TransformException("PID segment not found");

        Patient target = new Patient();

        setId(source, target);

        addNames(source, target);
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

        targetResources.addResource(target, ResourceTag.PatientSubject);
    }

    public void setId(AdtMessage source, Patient target) throws TransformException, MapperException {

        String patientIdentifierValue = getPatientIdentifierValue(source, HomertonConstants.primaryPatientIdentifierTypeCode);
        UUID patientUuid = mapper.mapPatientUuid(HomertonConstants.primaryPatientIdentifierTypeCode, patientIdentifierValue);

        target.setId(patientUuid.toString());
    }

    public static String getPatientIdentifierValue(AdtMessage message, String patientIdentifierTypeCode) {
        return PatientTransform.getAllPatientIdentifiers(message)
                .stream()
                .filter(t -> patientIdentifierTypeCode.equals(t.getIdentifierTypeCode()))
                .map(t -> t.getId())
                .collect(StreamExtension.firstOrNullCollector());
    }

    private static List<XpnInterface> getPatientNames(PidSegment pidSegment) {
        List<XpnInterface> names = new ArrayList<>();

        if (pidSegment.getPatientNames() != null)
            names.addAll(pidSegment.getPatientNames());

        if (pidSegment.getPatientAlias() != null)
            names.addAll(pidSegment.getPatientAlias());

        return names;
    }

    private static void addNames(AdtMessage source, Patient target) throws TransformException {

        List<HumanName> names = NameConverter.convert(getPatientNames(source.getPidSegment()));

        for (HumanName name : names)
            if (name != null)
                target.addName(name);
    }

    public static List<Cx> getAllPatientIdentifiers(AdtMessage source) {
        List<Cx> patientIdentifiers = new ArrayList<>();

        if (source.getPidSegment().getExternalPatientId() != null)
            patientIdentifiers.add(source.getPidSegment().getExternalPatientId());

        if (source.getPidSegment().getInternalPatientId() != null)
            patientIdentifiers.addAll(source.getPidSegment().getInternalPatientId());

        return patientIdentifiers;
    }

    private void addIdentifiers(AdtMessage source, Patient target) throws TransformException {
        List<Cx> identifiers = getAllPatientIdentifiers(source);

        for (Cx cx : identifiers) {
            Identifier identifier = IdentifierConverter.createIdentifier(cx, getResourceType());

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

    private void setPrimaryCareProvider(AdtMessage source, Patient target) throws MapperException, TransformException, ParseException {

        Pd1Segment pd1Segment = source.getPd1Segment();

        if (pd1Segment == null)
            return;

        Zpd zpd = pd1Segment.getFieldAsDatatype(HomertonConstants.homertonXpdPrimaryCarePd1FieldNumber, Zpd.class);

        OrganizationTransform organizationTransform = new OrganizationTransform(mapper, targetResources);
        Reference organisationReference = organizationTransform.createPrimaryCareProviderOrganisation(zpd);

        if (organisationReference != null)
            target.addCareProvider(organisationReference);

        PractitionerTransform practitionerTransform = new PractitionerTransform(mapper, targetResources);
        Reference practitionerReference = practitionerTransform.createPrimaryCarePractitioner(zpd, organisationReference);

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
        return AdministrativeGenderVs.convert(gender);
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
        target.setManagingOrganization(this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class));
    }
}
