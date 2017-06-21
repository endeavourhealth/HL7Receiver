package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Ce;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.datatypes.XpnInterface;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Nk1Segment;
import org.endeavourhealth.hl7parser.segments.PidSegment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.DateConverter;
import org.endeavourhealth.hl7transform.common.transform.PatientCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.common.converters.NameConverter;
import org.endeavourhealth.hl7transform.common.converters.TelecomConverter;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PatientTransform extends ResourceTransformBase {

    private static final Logger LOG = LoggerFactory.getLogger(PatientTransform.class);

    public PatientTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Patient;
    }

    public Patient transform(AdtMessage source) throws Exception {
        Validate.notNull(source);

        if (!source.hasPidSegment())
            throw new TransformException("PID segment not found");

        Patient target = new Patient();

        setId(source, target);

        PatientCommon.addNames(source, target, mapper);
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
        setPrimaryCareProvider(target);
        addPatientContacts(source, target);
        setManagingOrganization(source, target);

        return target;
    }

    public void setId(AdtMessage source, Patient target) throws TransformException, MapperException {

        String patientIdentifierValue = getBartsPrimaryPatientIdentifierValue(source);

        UUID patientUuid = mapper.getResourceMapper().mapPatientUuid(
                null,
                BartsConstants.primaryPatientIdentifierAssigningAuthority,
                patientIdentifierValue);

        target.setId(patientUuid.toString());
    }

    public static String getBartsPrimaryPatientIdentifierValue(AdtMessage source) {
        return PatientCommon.getPatientIdentifierValueByAssigningAuth(source, BartsConstants.primaryPatientIdentifierAssigningAuthority);
    }

    private void addIdentifiers(AdtMessage source, Patient target) throws TransformException, MapperException {
        List<Cx> identifiers = PatientCommon.getAllPatientIdentifiers(source);

        List<Identifier> targetIdentifiers = new ArrayList<>();

        for (Cx cx : identifiers) {
            Identifier identifier = IdentifierConverter.createIdentifier(cx, getResourceType(), mapper);

            if (identifier != null) {

                if (identifier.getSystem() != null)
                    if (identifier.getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER))
                        addTraceStatus(source.getPidSegment(), identifier);

            }

            if (!targetIdentifiers.stream().anyMatch(t -> StringUtils.equals(identifier.getSystem(), t.getSystem())))
                targetIdentifiers.add(identifier);
            else
                LOG.warn("More than one patient identifier exists with identifier system " + identifier.getSystem());
        }

        for (Identifier targetIdentifier : targetIdentifiers)
            target.addIdentifier(targetIdentifier);
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

    private void setPrimaryCareProvider(Patient target) throws MapperException, TransformException, ParseException {

        if (targetResources.hasResource(ResourceTag.MainPrimaryCareProviderOrganisation)) {
            Reference organisationReference = targetResources.getResourceReference(ResourceTag.MainPrimaryCareProviderOrganisation, Organization.class);
            target.addCareProvider(organisationReference);
        }

        if (targetResources.hasResource(ResourceTag.MainPrimaryCareProviderPractitioner)) {
            Reference practitionerReference = targetResources.getResourceReference(ResourceTag.MainPrimaryCareProviderPractitioner, Practitioner.class);
            target.addCareProvider(practitionerReference);
        }
    }

    private void addReligion(PidSegment sourcePid, Patient target) throws MapperException {
        if (sourcePid.getReligion() == null)
            return;

        CodeableConcept religion = mapper.getCodeMapper().mapReligion(sourcePid.getReligion().getAsString(), null);

        if (religion != null)
            target.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_RELIGION, religion));
    }

    private void addEthnicity(PidSegment sourcePid, Patient target) throws TransformException, MapperException {
        if (sourcePid.getEthnicGroups() == null)
            return;

        for (Ce ce : sourcePid.getEthnicGroups()) {
            if (ce == null)
                continue;

            CodeableConcept ethnicGroup = mapper.getCodeMapper().mapEthnicGroup(ce.getAsString(), null);

            if (ethnicGroup != null)
                target.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_ETHNICITY, ethnicGroup));
        }
    }

    private void addMaritalStatus(PidSegment sourcePid, Patient target) throws MapperException {
        if (sourcePid.getMaritalStatus() == null)
            return;

        CodeableConcept maritalStatus = mapper.getCodeMapper().mapMaritalStatus(sourcePid.getMaritalStatus().getAsString(), null);

        if (maritalStatus != null)
            target.setMaritalStatus(maritalStatus);
    }

    private void setAddress(AdtMessage source, Patient target) throws TransformException, MapperException {

        PidSegment pidSegment = source.getPidSegment();

        for (Address address : AddressConverter.convert(pidSegment.getAddresses(), mapper))
            if (address != null)
                target.addAddress(address);
    }

    private void setSex(PidSegment sourcePid, Patient target) throws TransformException, MapperException {
        Enumerations.AdministrativeGender gender = mapper.getCodeMapper().mapSex(sourcePid.getSex());

        if (gender != null)
            target.setGender(gender);
    }

    private void setContactPoint(PidSegment sourcePid, Patient target) throws TransformException, MapperException {
        for (ContactPoint cp : TelecomConverter.convert(sourcePid.getHomeTelephones(), this.mapper))
            target.addTelecom(cp);

        for (ContactPoint cp : TelecomConverter.convert(sourcePid.getBusinessTelephones(), this.mapper))
            target.addTelecom(cp);
    }

    private static void setDateOfBirth(PidSegment sourcePid, Patient target) throws ParseException, TransformException {
        if (sourcePid.getDateOfBirth() == null)
            return;

        target.setBirthDate(sourcePid.getDateOfBirth().asDate());

        if (sourcePid.getDateOfBirth().hasTimeComponent())
            target.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_BIRTH_DATE_TIME, DateConverter.getDateType(sourcePid.getDateOfBirth())));
    }

    private void setDateOfDeath(PidSegment sourcePid, Patient target) throws ParseException, TransformException, MapperException {
        if (sourcePid.getDateOfDeath() != null)
            target.setDeceased(DateConverter.getDateType(sourcePid.getDateOfDeath()));
        else if (isDeceased(sourcePid.getDeathIndicator()))
            target.setDeceased(new BooleanType(true));
    }

    private void setCommunication(PidSegment sourcePid, Patient target) throws ParseException, TransformException, MapperException {
        if (sourcePid.getPrimaryLanguage() == null)
            return;

        CodeableConcept primaryLanguage = mapper.getCodeMapper().mapPrimaryLanguage(sourcePid.getPrimaryLanguage().getAsString(), null);

        if (primaryLanguage != null) {
            target.addCommunication(new Patient.PatientCommunicationComponent()
                    .setLanguage(primaryLanguage)
                    .setPreferred(true));
        }
    }

    private boolean isDeceased(String deathIndicator) throws TransformException, MapperException {
        if (StringUtils.isEmpty(deathIndicator))
            return false;

        String mappedDeathIndicator = mapper.getCodeMapper().mapPatientDeathIndicator(deathIndicator);

        if (StringUtils.isEmpty(mappedDeathIndicator))
            return false;

        if (mappedDeathIndicator.equalsIgnoreCase("false"))
            return false;

        if (mappedDeathIndicator.equalsIgnoreCase("true"))
            return true;

        throw new TransformException(mappedDeathIndicator + " not recognised as a mapped death indicator code");
    }

    private void addPatientContacts(AdtMessage source, Patient target) throws TransformException, ParseException, MapperException {
        for (Nk1Segment nk1 : source.getNk1Segments())
            addPatientContact(nk1, target);
    }

    private void addPatientContact(Nk1Segment sourceNk1, Patient target) throws TransformException, ParseException, MapperException {

        if ((sourceNk1.getNKName().size() == 0)
            && sourceNk1.getPhoneNumber().size() == 0
            && sourceNk1.getBusinessPhoneNumber().size() == 0
            && sourceNk1.getAddresses().size() == 0)
            return;

        Patient.ContactComponent contactComponent = new Patient.ContactComponent();

        List<HumanName> names = NameConverter.convert(sourceNk1.getNKName(), mapper);

        for (HumanName name : names)
            contactComponent.setName(name);

        if (sourceNk1.getRelationship() != null)
            contactComponent.addRelationship(new CodeableConcept().setText(sourceNk1.getRelationship().getAsString()));

        for (ContactPoint cp : TelecomConverter.convert(sourceNk1.getPhoneNumber(), this.mapper))
            contactComponent.addTelecom(cp);

        for (ContactPoint cp : TelecomConverter.convert(sourceNk1.getBusinessPhoneNumber(), this.mapper))
            contactComponent.addTelecom(cp);

        //FHIR only allows 1 address but HL7v2 allows multiple addresses, this will currently only populate the last address.
        for (Address address : AddressConverter.convert(sourceNk1.getAddresses(), mapper))
            contactComponent.setAddress(address);

        Enumerations.AdministrativeGender gender = mapper.getCodeMapper().mapSex(sourceNk1.getSex());

        if (gender != null)
            contactComponent.setGender(gender);

        if (sourceNk1.getContactRole() != null)
            contactComponent.addRelationship(new CodeableConcept().setText((sourceNk1.getContactRole().getAsString())));

        if (sourceNk1.getDateTimeOfBirth() != null)
            contactComponent.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_CONTACT_DOB,
                    DateConverter.getDateType(sourceNk1.getDateTimeOfBirth())));

        if (sourceNk1.getPrimaryLanguage() != null)
            contactComponent.addExtension(ExtensionConverter.createStringExtension(FhirExtensionUri.PATIENT_CONTACT_MAIN_LANGUAGE, sourceNk1.getPrimaryLanguage().getAsString()));

        target.addContact(contactComponent);
    }

    private void setManagingOrganization(AdtMessage source, Patient target) throws MapperException, TransformException {
        target.setManagingOrganization(this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class));
    }
}
