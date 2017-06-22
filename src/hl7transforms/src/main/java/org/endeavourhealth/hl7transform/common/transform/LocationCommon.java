package org.endeavourhealth.hl7transform.common.transform;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.OrganisationClass;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;
import org.hl7.fhir.instance.model.valuesets.V3RoleCode;

import java.util.List;
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

    public static Reference createClassOfLocation(String classOfLocationName, Mapper mapper) throws MapperException {
        Validate.notEmpty(classOfLocationName, "classOfLocationName");

        Location location = new Location()
                .setName(classOfLocationName)
                .setMode(Location.LocationMode.KIND);

        UUID id = mapper.getResourceMapper().mapClassOfLocationUuid(classOfLocationName);
        location.setId(id.toString());

        return ReferenceHelper.createReference(ResourceType.Location, classOfLocationName);
    }

    public static UUID getId(Mapper mapper, String parentOdsSiteCode, List<String> locationNames) throws MapperException {
        return mapper.getResourceMapper().mapLocationUuid(parentOdsSiteCode, locationNames);
    }

    public static void setPartOf(Location location, Location partOfLocation) {
        location.setPartOf(ReferenceHelper.createReference(ResourceType.Location, partOfLocation.getId()));
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

    public static String getOdsSiteCode(Location location) {
        return location
                .getIdentifier()
                .stream()
                .filter(t -> FhirUri.IDENTIFIER_SYSTEM_ODS_CODE.equals(t.getSystem()))
                .map(t -> t.getValue())
                .collect(StreamExtension.firstOrNullCollector());
    }
}
