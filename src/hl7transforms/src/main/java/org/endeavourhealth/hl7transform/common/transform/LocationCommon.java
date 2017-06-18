package org.endeavourhealth.hl7transform.common.transform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.schema.OrganisationClass;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;
import org.hl7.fhir.instance.model.valuesets.V3RoleCode;

import java.util.UUID;

public class LocationCommon {

    public static Location createMainHospitalLocation(String odsSiteCode, Reference mainHospitalOrganisationReference, Mapper mapper) throws MapperException, TransformException {
        Validate.notEmpty(odsSiteCode, "odsSiteCode");

        MappedOrganisation mappedOrganisation = mapper.getOrganisationMapper().mapOrganisation(odsSiteCode);

        if (mappedOrganisation == null)
            throw new TransformException("Could not map HSCSite organisation from OdsSiteCode " + odsSiteCode + " when creating hospital location");

        if (mappedOrganisation.getOrganisationClass() != OrganisationClass.HSC_SITE)
            throw new TransformException("Trying to create a hospital location and OrganisationClass is not HSCSite");

        Location location = new Location()
                .setName(mappedOrganisation.getOrganisationName())
                .addIdentifier(IdentifierConverter.createOdsCodeIdentifier(mappedOrganisation.getOdsCode()))
                .setStatus(Location.LocationStatus.ACTIVE)
                .setAddress(AddressConverter.createWorkAddress(mappedOrganisation.getAddressLine1(), mappedOrganisation.getAddressLine2(), mappedOrganisation.getTown(), mappedOrganisation.getPostcode()))
                .setManagingOrganization(mainHospitalOrganisationReference)
                .setType(LocationCommon.createType(V3RoleCode.HOSP))
                .setPhysicalType(LocationCommon.createLocationPhysicalType(LocationPhysicalType.BU))
                .setMode(Location.LocationMode.INSTANCE);

        UUID id = mapper.getResourceMapper().mapLocationUuid(mappedOrganisation.getOdsCode());
        location.setId(id.toString());

        return location;
    }

    public static CodeableConcept createType(V3RoleCode v3RoleCode) {
        return new CodeableConcept()
                .addCoding(new Coding()
                        .setCode(v3RoleCode.toCode())
                        .setDisplay(v3RoleCode.getDisplay())
                        .setSystem(v3RoleCode.getSystem()));
    }

    public static CodeableConcept createLocationPhysicalType(LocationPhysicalType locationPhysicalType) {
        return new CodeableConcept()
                .addCoding(
                        new Coding()
                                .setCode(locationPhysicalType.toCode())
                                .setSystem(locationPhysicalType.getSystem())
                                .setDisplay(locationPhysicalType.getDisplay()));
    }
}
