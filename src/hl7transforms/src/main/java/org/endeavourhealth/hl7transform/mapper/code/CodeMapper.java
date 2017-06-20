package org.endeavourhealth.hl7transform.mapper.code;

import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.*;

public class CodeMapper extends CodeMapperBase {

    public CodeMapper(Mapper mapper) {
        super(mapper);
    }

    public Enumerations.AdministrativeGender mapSex(String sex) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_SEX,
                        sex,
                        (t) -> Enumerations.AdministrativeGender.fromCode(t),
                        (r) -> r.getSystem());
    }

    public HumanName.NameUse mapNameType(String nameType) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_NAME_TYPE,
                        nameType,
                        (t) -> HumanName.NameUse.fromCode(t),
                        (r) -> r.getSystem());
    }

    public Address.AddressUse mapAddressType(String addressType) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_ADDRESS_TYPE,
                        addressType,
                        (t) -> Address.AddressUse.fromCode(t),
                        (r) -> r.getSystem());
    }

    public ContactPoint.ContactPointSystem mapTelecomEquipmentType(String telecomEquipmentType) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_TELECOM_EQUIPMENT_TYPE,
                        telecomEquipmentType,
                        (t) -> ContactPoint.ContactPointSystem.fromCode(t),
                        (r) -> r.getSystem());
    }

    public ContactPoint.ContactPointUse mapTelecomUse(String telecomUse) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_TELECOM_USE,
                        telecomUse,
                        (t) -> ContactPoint.ContactPointUse.fromCode(t),
                        (r) -> r.getSystem());
    }

    public CodeableConcept mapPrimaryLanguage(String code, String term) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_PRIMARY_LANGUAGE,
                        code,
                        term);
    }

    public CodeableConcept mapEthnicGroup(String ethnicGroup) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_ETHNIC_GROUP,
                        "",
                        ethnicGroup);
    }

    public CodeableConcept mapMaritalStatus(String maritalStatus) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_MARITAL_STATUS,
                        "",
                        maritalStatus);
    }

    public CodeableConcept mapReligion(String religion) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_RELIGION,
                        "",
                        religion);
    }

    public Encounter.EncounterState mapAccountStatus(String accountStatus) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_ACCOUNT_STATUS,
                        accountStatus,
                        (t) -> Encounter.EncounterState.fromCode(t),
                        (r) -> r.getSystem());
    }

    public Encounter.EncounterClass mapPatientClass(String patientClass) throws MapperException {
        return this
                .mapCodeToEnum(
                        CodeContext.HL7_PATIENT_CLASS,
                        patientClass,
                        (t) -> Encounter.EncounterClass.fromCode(t),
                        (r) -> r.getSystem());
    }

    public CodeableConcept mapPatientType(String patientType) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_PATIENT_TYPE,
                        "",
                        patientType);
    }

    public CodeableConcept mapAdmissionType(String admissionType) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_ADMISSION_TYPE,
                        "",
                        admissionType);
    }

    public CodeableConcept mapDischargeDisposition(String dischargeDisposition) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_DISCHARGE_DISPOSITION,
                        "",
                        dischargeDisposition);
    }

    public CodeableConcept mapDischargeDestination(String dischargeDestination) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_DISCHARGED_TO_LOCATION,
                        "",
                        dischargeDestination);
    }

    public CodeableConcept mapMessageType(String messageType) throws MapperException {
        return this
                .mapToCodeableConcept(CodeContext.HL7_MESSAGE_TYPE,
                        messageType,
                        "");
    }

    public String mapPatientDeathIndicator(String patientDeathIndicator) throws MapperException {
        return this
                .mapToCode(CodeContext.HL7_PATIENT_DEATH_INDICATOR,
                        patientDeathIndicator);
    }

    public String mapPatientIdentifierTypeAndAssigningAuth(String assigningAuth, String patientIdentifierType) throws MapperException {
        return this
                .mapToCode(CodeContext.HL7_PATIENT_ID_TYPE_AND_ASSIGNING_AUTH,
                        assigningAuth + "^" + patientIdentifierType);
    }

    public String mapDoctorIdentifierTypeAndAssigningAuth(String assigningAuth, String doctorIdentifierType) throws MapperException {
        return this
                .mapToCode(CodeContext.HL7_DOCTOR_ID_TYPE_AND_ASSIGNING_AUTH,
                        assigningAuth + "^" + doctorIdentifierType);
    }

    public String mapEncounterIdentifierTypeAndAssigningAuth(String assigningAuth, String encounterIdentifierType) throws MapperException {
        return this
                .mapToCode(CodeContext.HL7_ENCOUNTER_ID_TYPE_AND_ASSIGNING_AUTH,
                        assigningAuth + "^" + encounterIdentifierType);
    }
}
