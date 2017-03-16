package org.endeavourhealth.hl7transform.transform.transforms;

import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.transform.ResourceContainer;
import org.endeavourhealth.hl7parser.datatypes.Pl;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;

import java.util.*;
import java.util.stream.Collectors;

public class LocationTransform {

    public static Reference transformAndGetReference(Pl source, Mapper mapper, ResourceContainer resourceContainer) throws MapperException {

        List<Pair<LocationPhysicalType, String>> locations = new ArrayList<>();

        locations.add(new Pair<>(LocationPhysicalType.BU, source.getBuilding()));
        locations.add(new Pair<>(LocationPhysicalType.WI, source.getPointOfCare()));
        locations.add(new Pair<>(LocationPhysicalType.RO, source.getRoom()));
        locations.add(new Pair<>(LocationPhysicalType.BD, source.getBed()));

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

            Location fhirLocation = createLocation(location, lastLocation, mapper);

            if (fhirLocation != null)
                if (!resourceContainer.hasResource(Location.class, fhirLocation.getId()))
                    resourceContainer.add(fhirLocation);

            lastLocation = fhirLocation;
        }

        if (lastLocation == null)
            return null;

        return ReferenceHelper.createReference(ResourceType.Location, lastLocation.getId());
    }

    private static Location createLocation(List<Pair<LocationPhysicalType, String>> locations, Location parentLocation, Mapper mapper) throws MapperException {
        if (locations.size() == 0)
            return null;

        String[] locationsNames = locations
                .stream()
                .map(t -> t.getValue())
                .collect(Collectors.toList())
                .toArray(new String[locations.size()]);

        String locationName = String.join(", ", locationsNames);
        UUID id = getId(locationsNames, mapper);

        Location location = new Location();

        location.setId(id.toString());
        location.setName(locationName);
        location.setMode(Location.LocationMode.INSTANCE);
        location.setPhysicalType(createLocationPhysicalType(locations.get(0).getKey()));

        if (parentLocation != null)
            location.setPartOf(ReferenceHelper.createReference(ResourceType.Location, parentLocation.getId()));

        return location;
    }

    private static UUID getId(String[] locationNames, Mapper mapper) throws MapperException {
        String uniqueIdentifyingString = "Location-" + StringUtils.deleteWhitespace(String.join("-", locationNames)).toUpperCase().replace(".", "");
        return mapper.mapResourceUuid(ResourceType.Location, uniqueIdentifyingString);
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
