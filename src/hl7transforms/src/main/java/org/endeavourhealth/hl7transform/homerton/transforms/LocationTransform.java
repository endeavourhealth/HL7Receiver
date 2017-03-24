package org.endeavourhealth.hl7transform.homerton.transforms;

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
import java.util.stream.Collectors;

public class LocationTransform extends TransformBase {

    public LocationTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Location;
    }

    public Reference createHomertonLocation() throws MapperException, TransformException, ParseException {

        final String odsCode = "RQXM1";
        final String locationName = "Homerton University Hospital";

        Location location = new Location()
                .setName(locationName)
                .addIdentifier(IdentifierConverter.createOdsCodeIdentifier(odsCode))
                .setStatus(Location.LocationStatus.ACTIVE)
                .setAddress(AddressConverter.createWorkAddress(Arrays.asList(OrganizationTransform.homertonAddressLine), OrganizationTransform.homertonCity, OrganizationTransform.homertonPostcode))
                .setManagingOrganization(this.targetResources.getManagingOrganisationReference())
                .setType(createType(V3RoleCode.HOSP))
                .setPhysicalType(createLocationPhysicalType(LocationPhysicalType.BU))
                .setMode(Location.LocationMode.INSTANCE);

        mapAndSetId(getUniqueIdentifyingString(odsCode, locationName), location);

        targetResources.addManagingLocation(location);

        return ReferenceHelper.createReference(ResourceType.Location, location.getId());
    }

    public Reference transformAndGetReference(Pl source) throws MapperException, TransformException, ParseException {

        List<Pair<LocationPhysicalType, String>> locations = new ArrayList<>();

        locations.add(new Pair<>(LocationPhysicalType.BU, source.getBuilding()));
        locations.add(new Pair<>(LocationPhysicalType.WI, source.getPointOfCare()));
        locations.add(new Pair<>(LocationPhysicalType.RO, source.getRoom()));
        locations.add(new Pair<>(LocationPhysicalType.BD, source.getBed()));

        Reference managingOrganisationReference = null;
        Location managingLocation = null;

        String facility = StringUtils.trim(StringUtils.defaultString(source.getFacility())).toLowerCase();

        if (facility.equals("homerton univer") || facility.equals("homerton uh")) {
            managingOrganisationReference = targetResources.getManagingOrganisationReference();
            managingLocation = targetResources.getManagingLocation();
        }

        locations = locations
                .stream()
                .filter(t -> StringUtils.isNotBlank(t.getValue()))
                .collect(Collectors.toList());

        Location lastLocation = null;

        for (int i = 1; i <= locations.size(); i++) {
            List<Pair<LocationPhysicalType, String>> location = Lists.reverse(locations
                    .stream()
                    .limit(i)
                    .collect(Collectors.toList()));

            Location fhirLocation = createLocation(location, lastLocation, managingLocation, managingOrganisationReference);

            if (fhirLocation != null)
                targetResources.addResource(fhirLocation);

            lastLocation = fhirLocation;
        }

        if (lastLocation == null)
            return null;

        return ReferenceHelper.createReference(ResourceType.Location, lastLocation.getId());
    }

    private Location createLocation(List<Pair<LocationPhysicalType, String>> locations,
                                    Location directParentLocation,
                                    Location topParentLocation,
                                    Reference managingOrganisation) throws MapperException, TransformException, ParseException {
        if (locations.size() == 0)
            return null;

        String locationName = locations.get(0).getValue();

        String[] locationsNames = locations
                .stream()
                .map(t -> t.getValue())
                .collect(Collectors.toList())
                .toArray(new String[locations.size()]);

        String locationDescription = String.join(", ", locationsNames);

        Location location = new Location();

        mapAndSetId(getUniqueIdentifyingString(getOdsSiteCode(topParentLocation), topParentLocation.getName(), locationsNames), location);

        location.setName(locationName);
        location.setDescription(locationDescription);
        location.setMode(Location.LocationMode.INSTANCE);
        location.setPhysicalType(createLocationPhysicalType(locations.get(0).getKey()));

        if (managingOrganisation != null)
            location.setManagingOrganization(managingOrganisation);

        if (directParentLocation != null)
            setPartOf(location, directParentLocation);
        else if (topParentLocation != null)
            setPartOf(location, topParentLocation);

        return location;
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

    private static String getUniqueIdentifyingString(String odsSiteCode, String locationName) {
        Validate.notBlank(odsSiteCode, "odsSiteCode");
        Validate.notBlank(locationName, "locationName");

        return createIdentifyingString(ImmutableMap.of(
                "OdsSiteCode", odsSiteCode,
                "LocationName", locationName.replace(".", "")
        ));
    }

    private static String getUniqueIdentifyingString(String parentOdsSiteCode, String parentLocationName, String[] locationNames) {
        Validate.notBlank(parentOdsSiteCode, "parentOdsSiteCode");
        Validate.notBlank(parentLocationName, "parentLocationName");
        Validate.notBlank(StringUtils.join(locationNames, ""), "locationNames");

        return createIdentifyingString(ImmutableMap.of(
                "ParentOdsSiteCode", parentOdsSiteCode,
                "ParentLocationName", parentLocationName.replace(".", ""),
                "LocationNameHierarchy", StringUtils.join(locationNames, repeatingValueSeperator)
        ));
    }
}
