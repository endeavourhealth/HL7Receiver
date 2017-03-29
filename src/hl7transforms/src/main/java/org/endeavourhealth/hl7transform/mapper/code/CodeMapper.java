package org.endeavourhealth.hl7transform.mapper.code;

import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.ContactPoint;
import org.hl7.fhir.instance.model.Enumerations;
import org.hl7.fhir.instance.model.HumanName;

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
}
