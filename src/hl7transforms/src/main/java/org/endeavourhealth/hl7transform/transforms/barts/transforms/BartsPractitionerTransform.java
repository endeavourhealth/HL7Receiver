package org.endeavourhealth.hl7transform.transforms.barts.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pd1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.NameConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.transforms.barts.constants.BartsConstants;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.UUID;

public class BartsPractitionerTransform extends ResourceTransformBase {

    public BartsPractitionerTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Practitioner;
    }

    public Practitioner createMainPrimaryCareProviderPractitioner(AdtMessage adtMessage) throws MapperException, TransformException, ParseException {
        Pd1Segment pd1Segment = adtMessage.getPd1Segment();

        if (pd1Segment == null)
            return null;

        List<Xcn> xcns = pd1Segment.getPatientPrimaryCareProvider();

        if (xcns.size() == 0)
            return null;

        if (xcns.size() > 1)
            throw new TransformException("More than one patient primary care provider");

        Xcn xcn = xcns.get(0);

        if (StringUtils.isEmpty(xcn.getFamilyName()))
            return null;

        Practitioner practitioner = new Practitioner();

        if (StringUtils.isNotBlank(xcn.getId())) {
            String gmcCode = StringUtils.deleteWhitespace(xcn.getId()).toUpperCase();

            if (gmcCode.startsWith("G")) {
                practitioner.addIdentifier(new Identifier()
                        .setSystem(FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER)
                        .setValue(gmcCode));
            }
        }

        practitioner.setName(NameConverter.createOfficialName(xcn.getFamilyName(), xcn.getGivenName(), xcn.getMiddleName(), null));

        Organization primaryCareOrganization = targetResources.getResourceSingleOrNull(ResourceTag.MainPrimaryCareProviderOrganisation, Organization.class);

        if (primaryCareOrganization != null) {
            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent()
                    .setManagingOrganization(ReferenceHelper.createReferenceExternal(primaryCareOrganization)));
        }

        UUID id = getId(practitioner, primaryCareOrganization);
        practitioner.setId(id.toString());

        return practitioner;
    }

    private String getIdentifierValue(List<Identifier> identifiers, String system) {
        Identifier identifier = getIdentifierWithSystem(identifiers, system);

        if (identifier == null)
            return null;

        return identifier.getValue();
    }

    private Identifier getIdentifierWithSystem(List<Identifier> identifiers, String system) {
        if (identifiers == null)
            return null;

        return identifiers
                .stream()
                .filter(t -> t.getSystem().equalsIgnoreCase(system))
                .collect(StreamExtension.firstOrNullCollector());
    }

    private UUID getId(Practitioner source, Organization sourceRoleOrganisation) throws MapperException, TransformException {

        String forename = NameConverter.getFirstGivenName(source.getName());
        String surname = NameConverter.getFirstSurname(source.getName());
        String primaryIdentifier = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PRIMARY_PRACTITIONER_ID);
        String consultantCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE);
        String gmcCode = getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER);
        String odsCode = "";

        if (sourceRoleOrganisation != null)
            odsCode = getIdentifierValue(sourceRoleOrganisation.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);

        if (StringUtils.isBlank(primaryIdentifier)
            && StringUtils.isBlank(consultantCode)
                && StringUtils.isBlank(gmcCode)) {

            return mapper.getResourceMapper().mapPractitionerUuid(surname, forename, odsCode);
        }

        return mapper.getResourceMapper().mapPractitionerUuid(surname, forename, BartsConstants.primaryPractitionerIdentifierTypeCode, primaryIdentifier, consultantCode, gmcCode);
    }
}
