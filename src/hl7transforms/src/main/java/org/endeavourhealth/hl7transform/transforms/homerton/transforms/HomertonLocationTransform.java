package org.endeavourhealth.hl7transform.transforms.homerton.transforms;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.common.*;
import org.endeavourhealth.hl7transform.common.transform.LocationCommon;
import org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;

import java.util.*;

public class HomertonLocationTransform extends ResourceTransformBase {

    public HomertonLocationTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Location;
    }

    public Location createHomertonHospitalLocation() throws MapperException, TransformException, ParseException {
        Reference mainHospitalOrganisationReference = this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class);
        return LocationCommon.createMainHospitalLocation(HomertonConstants.odsSiteCodeHomerton, mainHospitalOrganisationReference, mapper);
    }

    public Location createStLeonardsHospitalLocation() throws MapperException, TransformException, ParseException {
        Reference mainHospitalOrganisationReference = this.targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class);
        return LocationCommon.createMainHospitalLocation(HomertonConstants.odsSiteCodeStLeonards, mainHospitalOrganisationReference, mapper);
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

            saveToTargetResources(fhirLocation);

            directParentLocation = fhirLocation;
            locationParentNames.add(0, locationName);
        }

        return ReferenceHelper.createReference(ResourceType.Location, directParentLocation.getId());
    }

    public Location getHospitalBuildingLocation(Pl source) throws TransformException, ParseException, MapperException {
        if (StringUtils.isBlank(source.getBuilding()))
            return targetResources.getResourceSingle(ResourceTag.MainHospitalLocation, Location.class);

        if (HomertonConstants.locationBuildingHomerton.equalsIgnoreCase(StringUtils.trim(source.getBuilding())))
            return targetResources.getResourceSingle(ResourceTag.MainHospitalLocation, Location.class);

        if (HomertonConstants.locationBuildingStLeonards.equalsIgnoreCase(StringUtils.trim(source.getBuilding()))) {
            Location stLeonardsHospitalLocation = createStLeonardsHospitalLocation();

            saveToTargetResources(stLeonardsHospitalLocation);

            return stLeonardsHospitalLocation;
        }

        throw new TransformException("Building of " + source.getBuilding() + " not recognised");
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

        UUID id = LocationCommon.getId(mapper, LocationCommon.getOdsSiteCode(topParentBuildingLocation), locationHierarchy);
        location.setId(id.toString());

        location.setName(locationName);

        location.setDescription(String.join(", ", ImmutableList
                .<String>builder()
                .addAll(locationHierarchy)
                .add(topParentBuildingLocation.getName())
                .build()));

        location.setMode(Location.LocationMode.INSTANCE);
        location.setPhysicalType(LocationCommon.createLocationPhysicalType(locationPhysicalType));

        if (managingOrganisation != null)
            location.setManagingOrganization(managingOrganisation);

        LocationCommon.setPartOf(location, directParentLocation);

        return location;
    }
}
