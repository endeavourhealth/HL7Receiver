package org.endeavourhealth.hl7transform.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.common.TransformException;
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

public class OrganizationTransform extends TransformBase {

    public OrganizationTransform(Mapper mapper, ResourceContainer resourceContainer) {
        super(mapper, resourceContainer);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Organization;
    }

    public Reference createHomertonManagingOrganisation() throws MapperException, TransformException, ParseException {
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

        mapAndSetId(getUniqueIdentifyingString(odsCode, organisationName), organization);

        targetResources.addManagingOrganisation(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    public Reference createGeneralPracticeOrganisation(String odsCode, String practiceName, List<String> addressLines, String city, String postcode, String phoneNumber) throws MapperException, TransformException, ParseException {

        if (StringUtils.isBlank(practiceName))
            return null;

        Organization organization = new Organization();

        mapAndSetId(getUniqueIdentifyingString(odsCode, practiceName), organization);

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

        targetResources.addResource(organization);

        return ReferenceHelper.createReference(ResourceType.Organization, organization.getId());
    }

    private String getUniqueIdentifyingString(String odsCode, String name) throws TransformException {

        if (odsCode == null)
            odsCode = "";

        odsCode = StringUtils.deleteWhitespace(odsCode).toUpperCase();
        name = StringUtils.deleteWhitespace(name).toUpperCase();

        if (StringUtils.isBlank(odsCode) && StringUtils.isBlank(name))
            throw new TransformException("ODS code and organisation name are blank");

        return odsCode + "-" + name;
    }
}
