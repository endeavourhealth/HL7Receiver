package org.endeavourhealth.hl7transform.transforms.homerton.transforms;

import com.google.common.collect.ImmutableList;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;
import org.hl7.fhir.instance.model.valuesets.V3RoleCode;

import java.util.*;

public class LocationTransform extends ResourceTransformBase {

    public LocationTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Location;
    }

    public Location createHomertonHospitalLocation() throws MapperException, TransformException, ParseException {

        Location location = new Location()
                .setName(HomertonConstants.locationName)
                .addIdentifier(IdentifierConverter.createOdsCodeIdentifier(HomertonConstants.odsSiteCode))
                .setStatus(Location.LocationStatus.ACTIVE)
                .setAddress(AddressConverter.createWorkAddress(HomertonConstants.addressLine, null, HomertonConstants.addressCity, HomertonConstants.addressPostcode))
                .setManagingOrganization(this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class))
                .setType(createType(V3RoleCode.HOSP))
                .setPhysicalType(createLocationPhysicalType(LocationPhysicalType.BU))
                .setMode(Location.LocationMode.INSTANCE);

        UUID id = getId(HomertonConstants.odsSiteCode, HomertonConstants.locationName);
        location.setId(id.toString());

        return location;
    }

    public Location createStLeonardsHospitalLocation() throws MapperException, TransformException, ParseException {
        Location location = new Location()
                .setName(HomertonConstants.StLeonardsConstants.locationName)
                .addIdentifier(IdentifierConverter.createOdsCodeIdentifier(HomertonConstants.StLeonardsConstants.odsSiteCode))
                .setStatus(Location.LocationStatus.ACTIVE)
                .setAddress(AddressConverter.createWorkAddress(HomertonConstants.StLeonardsConstants.addressLine, null, HomertonConstants.StLeonardsConstants.addressCity, HomertonConstants.StLeonardsConstants.addressPostcode))
                .setManagingOrganization(this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class))
                .setType(createType(V3RoleCode.HOSP))
                .setPhysicalType(createLocationPhysicalType(LocationPhysicalType.BU))
                .setMode(Location.LocationMode.INSTANCE);

        UUID id = getId(HomertonConstants.StLeonardsConstants.locationBuilding, HomertonConstants.StLeonardsConstants.locationName);
        location.setId(id.toString());

        return location;
    }

    public Reference createHomertonConstituentLocation(Pl source) throws MapperException, TransformException, ParseException {

        if (StringUtils.isBlank(source.getFacility())
                && StringUtils.isBlank(source.getBuilding())
                && StringUtils.isBlank(source.getPointOfCare())
                && StringUtils.isBlank(source.getRoom())
                && StringUtils.isBlank(source.getBed()))
            return null;

        if (!HomertonConstants.locationFacility.equalsIgnoreCase(StringUtils.trim(source.getFacility())))
            throw new TransformException("Location facility of " + source.getFacility() + " not recognised");

        if (StringUtils.isBlank(source.getBuilding())
                && StringUtils.isBlank(source.getPointOfCare())
                && StringUtils.isBlank(source.getRoom())
                && StringUtils.isBlank(source.getBed()))
            return null;

        Reference managingOrganisationReference = targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class);
        Location topParentBuildingLocation = getHospitalBuildingLocation(source);

        List<Pair<LocationPhysicalType, String>> locations = new ArrayList<>();

        if (StringUtils.isNotBlank(source.getPointOfCare()))
            locations.add(new Pair<>(LocationPhysicalType.WI, source.getPointOfCare()));

        if (StringUtils.isNotBlank(source.getRoom()))
            locations.add(new Pair<>(LocationPhysicalType.RO, source.getRoom()));

        if (StringUtils.isNotBlank(source.getBed()))
            locations.add(new Pair<>(LocationPhysicalType.BD, source.getBed()));

        Location directParentLocation = topParentBuildingLocation;

        List<String> locationParentNames = new ArrayList<>();

        for (int i = 0; i < locations.size(); i++) {

            String locationName = locations.get(i).getValue();
            LocationPhysicalType locationPhysicalType = locations.get(i).getKey();

            Location fhirLocation = createLocationFromPl(locationName, locationPhysicalType, locationParentNames, directParentLocation, topParentBuildingLocation, managingOrganisationReference);

            if (fhirLocation != null)
                if (!targetResources.hasResource(fhirLocation.getId()))
                    targetResources.addResource(fhirLocation, null);

            directParentLocation = fhirLocation;
            locationParentNames.add(0, locationName);
        }

        return ReferenceHelper.createReference(ResourceType.Location, directParentLocation.getId());
    }

    public Location getHospitalBuildingLocation(Pl source) throws TransformException, ParseException, MapperException {
        if (StringUtils.isBlank(source.getBuilding()))
            return targetResources.getResourceSingle(ResourceTag.MainHospitalLocation, Location.class);

        if (HomertonConstants.locationBuilding.equalsIgnoreCase(StringUtils.trim(source.getBuilding())))
            return targetResources.getResourceSingle(ResourceTag.MainHospitalLocation, Location.class);

        if (HomertonConstants.StLeonardsConstants.locationBuilding.equalsIgnoreCase(StringUtils.trim(source.getBuilding()))) {
            Location stLeonardsHospitalLocation = createStLeonardsHospitalLocation();

            if (stLeonardsHospitalLocation != null)
                if (!targetResources.hasResource(stLeonardsHospitalLocation.getId()))
                    targetResources.addResource(stLeonardsHospitalLocation, null);

            return stLeonardsHospitalLocation;
        }

        throw new TransformException("Building of " + source.getBuilding() + " not recognised");
    }

    public Reference createClassOfLocation(String classOfLocationName) throws MapperException {
        Validate.notEmpty(classOfLocationName, "classOfLocationName");

        Location location = new Location()
                .setName(classOfLocationName)
                .setMode(Location.LocationMode.KIND);

        UUID id = getId(classOfLocationName);
        location.setId(id.toString());

        return ReferenceHelper.createReference(ResourceType.Location, classOfLocationName);
    }

    private Location createLocationFromPl(String locationName,
                                          LocationPhysicalType locationPhysicalType,
                                          List<String> locationParentNames,
                                          Location directParentLocation,
                                          Location topParentBuildingLocation,
                                          Reference managingOrganisation) throws MapperException, TransformException, ParseException {

        Validate.notNull(locationName, "locationName");
        Validate.notNull(locationPhysicalType, "locationPhysicalType");
        Validate.notNull(locationParentNames, "locationParentNames");
        Validate.notNull(directParentLocation, "directParentLocation");
        Validate.notNull(topParentBuildingLocation, "topParentBuildingLocation");
        Validate.notNull(managingOrganisation, "managingOrganisation");

        Location location = new Location();

        List<String> locationHierarchy = ImmutableList
                .<String>builder()
                .add(locationName)
                .addAll(locationParentNames)
                .build();

        UUID id = getId(getOdsSiteCode(topParentBuildingLocation), topParentBuildingLocation.getName(), locationHierarchy);
        location.setId(id.toString());

        location.setName(locationName);

        location.setDescription(String.join(", ", ImmutableList
                .<String>builder()
                .addAll(locationHierarchy)
                .add(topParentBuildingLocation.getName())
                .build()));

        location.setMode(Location.LocationMode.INSTANCE);
        location.setPhysicalType(createLocationPhysicalType(locationPhysicalType));

        if (managingOrganisation != null)
            location.setManagingOrganization(managingOrganisation);

        setPartOf(location, directParentLocation);

        return location;
    }

    private UUID getId(String classOfLocationName) throws MapperException {
        return mapper.getResourceMapper().mapLocationUuid(classOfLocationName);
    }

    private UUID getId(String odsSiteCode, String locationName) throws MapperException {
        return mapper.getResourceMapper().mapLocationUuid(odsSiteCode, locationName);
    }

    public UUID getId(String parentOdsSiteCode, String parentLocationName, List<String> locationNames) throws MapperException {
        return mapper.getResourceMapper().mapLocationUuid(parentOdsSiteCode, parentLocationName, locationNames);
    }

    private static String getOdsSiteCode(Location location) {
        return location
                .getIdentifier()
                .stream()
                .filter(t -> FhirUri.IDENTIFIER_SYSTEM_ODS_CODE.equals(t.getSystem()))
                .map(t -> t.getValue())
                .collect(StreamExtension.firstOrNullCollector());
    }

    private static void setPartOf(Location location, Location partOfLocation) {
        location.setPartOf(ReferenceHelper.createReference(ResourceType.Location, partOfLocation.getId()));
    }

    private static CodeableConcept createType(V3RoleCode v3RoleCode) {
        return new CodeableConcept()
                .addCoding(new Coding()
                        .setCode(v3RoleCode.toCode())
                        .setDisplay(v3RoleCode.getDisplay())
                        .setSystem(v3RoleCode.getSystem()));
    }

    private static CodeableConcept createLocationPhysicalType(LocationPhysicalType locationPhysicalType) {
        return new CodeableConcept()
                .addCoding(
                        new Coding()
                                .setCode(locationPhysicalType.toCode())
                                .setSystem(locationPhysicalType.getSystem())
                                .setDisplay(locationPhysicalType.getDisplay()));
    }
}
