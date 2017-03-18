package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.common.converters.TelecomConverter;
import org.hl7.fhir.instance.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class OrganizationTransform {

    private Mapper mapper;
    private ResourceContainer resourceContainer;

    public OrganizationTransform(Mapper mapper, ResourceContainer resourceContainer) {
        this.mapper = mapper;
        this.resourceContainer = resourceContainer;
    }

    public Reference createHomertonManagingOrganisation() throws MapperException {
        final String organisationName = "Homerton University Hospital NHS Foundation Trust";
        final String odsCode = "RQX";

        Organization organization = new Organization()
                .setName(organisationName)
                .addAddress(AddressConverter
                        .createWorkAddress(Arrays.asList("Homerton Row"), "London", "E9 6SR"))
                .addIdentifier(new Identifier()
                        .setSystem(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE)
                        .setValue(odsCode))
                .setType(new CodeableConcept()
                        .addCoding(new Coding()
                                .setCode(OrganisationType.NHS_TRUST.getCode())
                                .setSystem(OrganisationType.NHS_TRUST.getSystem())
                                .setDisplay(OrganisationType.NHS_TRUST.getDescription())));


        UUID id = getId(odsCode, organisationName);
        organization.setId(id.toString());

        resourceContainer.addManagingOrganisation(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    public Reference createGeneralPracticeOrganisation(String odsCode, String practiceName, List<String> addressLines, String city, String postcode, String phoneNumber) throws MapperException {

        if (StringUtils.isBlank(practiceName))
            return null;

        Organization organization = new Organization();

        if (StringUtils.isNotBlank(odsCode)) {
            organization.addIdentifier(new Identifier()
                    .setSystem(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE)
                    .setValue(StringUtils.deleteWhitespace(odsCode).toUpperCase()));
        }

        organization.setName(StringHelper.formatName(practiceName));

        if (StringUtils.isNotBlank(phoneNumber))
            organization.addTelecom(TelecomConverter.createWorkPhone(phoneNumber));

        Address address = AddressConverter.createWorkAddress(addressLines, city, postcode);

        if (address != null)
            organization.addAddress(address);

        organization.setType(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(OrganisationType.GP_PRACTICE.getSystem())
                        .setDisplay(OrganisationType.GP_PRACTICE.getDescription())
                        .setCode(OrganisationType.GP_PRACTICE.getCode())));


        UUID id = getId(odsCode, practiceName);

        organization.setId(id.toString());

        resourceContainer.addResource(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    private UUID getId(String odsCode, String name) throws MapperException {

        if (odsCode == null)
            odsCode = "";

        odsCode = StringUtils.deleteWhitespace(odsCode).toUpperCase();
        name = StringUtils.deleteWhitespace(name).toUpperCase();

        String uniqueIdentifyingString = "Organization-" + odsCode + "-" + name;

        return mapper.mapResourceUuid(ResourceType.Organization, uniqueIdentifyingString);
    }
}
