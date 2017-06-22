package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import com.google.common.collect.ImmutableList;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.transform.LocationCommon;
import org.endeavourhealth.hl7transform.common.transform.OrganisationCommon;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BartsLocationTransform extends ResourceTransformBase {

    public BartsLocationTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Location;
    }

    public Location createBartsFacility(String facility, Reference managingOrganisationReference) throws MapperException, TransformException {
        String odsSiteCode = mapper.getCodeMapper().mapLocationFacility(facility);

        if (StringUtils.isBlank(odsSiteCode))
            throw new TransformException("odsSiteCode not found for location facility " + facility);

        Location location = LocationCommon.createMainHospitalLocation(odsSiteCode, managingOrganisationReference, mapper);

        if (location == null)
            throw new TransformException("Could not create location facility " + facility);

        saveToTargetResources(location);

        return location;
    }

    public Reference createBartsConstituentLocation(Pl source) throws MapperException, TransformException, ParseException {
        if (StringUtils.isBlank(source.getFacility())
                && StringUtils.isBlank(source.getBuilding())
                && StringUtils.isBlank(source.getPointOfCare())
                && StringUtils.isBlank(source.getRoom())
                && StringUtils.isBlank(source.getBed()))
            return null;

        Reference managingOrganisationReference = targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class);
        Location locationFacility = createBartsFacility(source.getFacility(), managingOrganisationReference);

        if (StringUtils.isBlank(source.getBuilding())
                && StringUtils.isBlank(source.getPointOfCare())
                && StringUtils.isBlank(source.getRoom())
                && StringUtils.isBlank(source.getBed()))
            return ReferenceHelper.createReference(ResourceType.Location, locationFacility.getId());

        List<Pair<LocationPhysicalType, String>> locations = new ArrayList<>();

        if (StringUtils.isNotBlank(source.getBuilding()))
            locations.add(new Pair<>(LocationPhysicalType.BU, source.getBuilding()));

        if (StringUtils.isNotBlank(source.getPointOfCare()))
            locations.add(new Pair<>(LocationPhysicalType.WI, source.getPointOfCare()));

        if (StringUtils.isNotBlank(source.getRoom()))
            locations.add(new Pair<>(LocationPhysicalType.RO, source.getRoom()));

        if (StringUtils.isNotBlank(source.getBed()))
            locations.add(new Pair<>(LocationPhysicalType.BD, source.getBed()));

        Location directParentLocation = locationFacility;

        List<String> locationParentNames = new ArrayList<>();

        for (int i = 0; i < locations.size(); i++) {

            String locationName = locations.get(i).getValue();
            LocationPhysicalType locationPhysicalType = locations.get(i).getKey();

            Location fhirLocation = createLocationFromPl(locationName, locationPhysicalType, locationParentNames, directParentLocation, locationFacility, managingOrganisationReference);

            saveToTargetResources(fhirLocation);

            directParentLocation = fhirLocation;
            locationParentNames.add(0, locationName);
        }

        return ReferenceHelper.createReference(ResourceType.Location, directParentLocation.getId());
    }

    private Location createLocationFromPl(String locationName,
                                          LocationPhysicalType locationPhysicalType,
                                          List<String> locationParentNames,
                                          Location directParentLocation,
                                          Location topParentFacilityLocation,
                                          Reference managingOrganisation) throws MapperException, TransformException, ParseException {

        Validate.notNull(locationName, "locationName");
        Validate.notNull(locationPhysicalType, "locationPhysicalType");
        Validate.notNull(locationParentNames, "locationParentNames");
        Validate.notNull(directParentLocation, "directParentLocation");
        Validate.notNull(topParentFacilityLocation, "topParentFacilityLocation");
        Validate.notNull(managingOrganisation, "managingOrganisation");

        Location location = new Location();

        List<String> locationHierarchy = ImmutableList
                .<String>builder()
                .add(locationName)
                .addAll(locationParentNames)
                .build();

        UUID id = LocationCommon.getId(mapper, LocationCommon.getOdsSiteCode(topParentFacilityLocation), topParentFacilityLocation.getName(), locationHierarchy);
        location.setId(id.toString());

        location.setName(locationName);

        location.setDescription(String.join(", ", ImmutableList
                .<String>builder()
                .addAll(locationHierarchy)
                .add(topParentFacilityLocation.getName())
                .build()));

        location.setMode(Location.LocationMode.INSTANCE);
        location.setPhysicalType(LocationCommon.createLocationPhysicalType(locationPhysicalType));

        if (managingOrganisation != null)
            location.setManagingOrganization(managingOrganisation);

        LocationCommon.setPartOf(location, directParentLocation);

        return location;
    }
}
