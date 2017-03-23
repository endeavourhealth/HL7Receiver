package org.endeavourhealth.hl7transform.homerton.transforms;

import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.hl7parser.Helpers;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;
import org.hl7.fhir.instance.model.valuesets.V3RoleCode;
import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

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
                .addIdentifier(new Identifier()
                        .setSystem(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE)
                        .setValue(odsCode))
                .setStatus(Location.LocationStatus.ACTIVE)
                .setAddress(AddressConverter.createWorkAddress(Arrays.asList("Homerton Row"), "London", "E9 6SR"))
                .setManagingOrganization(this.targetResources.getManagingOrganisation())
                .setType(new CodeableConcept()
                        .addCoding(new Coding()
                                .setCode(V3RoleCode.HOSP.toCode())
                                .setDisplay(V3RoleCode.HOSP.getDisplay())
                                .setSystem(V3RoleCode.HOSP.getSystem())))
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

        String facility = StringUtils.trim(StringUtils.defaultString(source.getFacility())).toLowerCase();

        Reference managingOrganisation = null;

        if (facility.equals("homerton univer") || facility.equals("homerton uh"))
            managingOrganisation = targetResources.getManagingOrganisation();

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

            Location fhirLocation = createLocation(location, lastLocation, managingOrganisation);

            if (fhirLocation != null)
                targetResources.addResource(fhirLocation);

            lastLocation = fhirLocation;
        }

        if (lastLocation == null)
            return null;

        return ReferenceHelper.createReference(ResourceType.Location, lastLocation.getId());
    }

    private Location createLocation(List<Pair<LocationPhysicalType, String>> locations,
                                           Location parentLocation,
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

        mapAndSetId(getUniqueIdentifyingString(locationsNames), location);

        location.setName(locationName);
        location.setDescription(locationDescription);
        location.setMode(Location.LocationMode.INSTANCE);
        location.setPhysicalType(createLocationPhysicalType(locations.get(0).getKey()));

        if (managingOrganisation != null)
            location.setManagingOrganization(managingOrganisation);

        if (parentLocation != null)
            location.setPartOf(ReferenceHelper.createReference(ResourceType.Location, parentLocation.getId()));

        return location;
    }

    private static String getUniqueIdentifyingString(String odsCode, String locationName) {

        odsCode = StringUtils.upperCase(StringUtils.deleteWhitespace(odsCode));
        locationName = StringUtils.remove(StringUtils.upperCase(StringUtils.deleteWhitespace(locationName).toUpperCase()), ".");

        return "OdsCode=" + odsCode + "-" + locationName;
    }

    private static String getUniqueIdentifyingString(String[] locationNames) {
        return getUniqueIdentifyingString("", StringUtils.join(locationNames, "-"));
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
