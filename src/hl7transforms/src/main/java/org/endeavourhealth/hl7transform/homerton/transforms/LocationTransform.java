package org.endeavourhealth.hl7transform.homerton.transforms;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;
import org.hl7.fhir.instance.model.valuesets.V3RoleCode;

import java.util.*;

public class LocationTransform extends TransformBase {

    public LocationTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Location;
    }

    public Reference createHomertonHospitalLocation() throws MapperException, TransformException, ParseException {

        Location location = new Location()
                .setName(HomertonConstants.locationName)
                .addIdentifier(IdentifierConverter.createOdsCodeIdentifier(HomertonConstants.odsSiteCode))
                .setStatus(Location.LocationStatus.ACTIVE)
                .setAddress(AddressConverter.createWorkAddress(Arrays.asList(HomertonConstants.addressLine), HomertonConstants.addressCity, HomertonConstants.addressPostcode))
                .setManagingOrganization(this.targetResources.getManagingOrganisationReference())
                .setType(createType(V3RoleCode.HOSP))
                .setPhysicalType(createLocationPhysicalType(LocationPhysicalType.BU))
                .setMode(Location.LocationMode.INSTANCE);

        UUID id = getId(HomertonConstants.odsSiteCode, HomertonConstants.locationName);
        location.setId(id.toString());

        targetResources.addManagingLocation(location);

        return ReferenceHelper.createReference(ResourceType.Location, location.getId());
    }

    public Reference createHomertonConstituentLocation(Pl source) throws MapperException, TransformException, ParseException {

        if (StringUtils.isBlank(source.getAsString()))
            return null;

        if (!HomertonConstants.locationFacility.equalsIgnoreCase(StringUtils.trim(source.getFacility())))
            throw new TransformException("Location facility of " + source.getFacility() + " not recognised");

        if (!HomertonConstants.locationBuilding.equalsIgnoreCase(StringUtils.trim(source.getBuilding())))
            throw new TransformException("Building of " + source.getBuilding() + " not recognised");

        Reference managingOrganisationReference = targetResources.getManagingOrganisationReference();
        Location topParentBuildingLocation = targetResources.getManagingLocation();

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
                targetResources.addResource(fhirLocation);

            directParentLocation = fhirLocation;
            locationParentNames.add(0, locationName);
        }

        return ReferenceHelper.createReference(ResourceType.Location, directParentLocation.getId());
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

    private UUID getId(String odsSiteCode, String locationName) throws MapperException {
        return mapper.mapLocationUuid(odsSiteCode, locationName);
    }

    public UUID getId(String parentOdsSiteCode, String parentLocationName, List<String> locationNames) throws MapperException {
        return mapper.mapLocationUuid(parentOdsSiteCode, parentLocationName, locationNames);
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
