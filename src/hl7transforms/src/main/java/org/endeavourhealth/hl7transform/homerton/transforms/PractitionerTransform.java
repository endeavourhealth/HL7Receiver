package org.endeavourhealth.hl7transform.homerton.transforms;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.homerton.transforms.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.MapperException;
import org.endeavourhealth.hl7parser.datatypes.Xcn;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.endeavourhealth.hl7transform.common.converters.NameConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class PractitionerTransform extends TransformBase {

    public PractitionerTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Practitioner;
    }

    public Reference createPrimaryCarePractitioner(String gmcCode, String surname, String forenames, Reference primaryCareOrganizationReference) throws MapperException, TransformException, ParseException {
        Practitioner practitioner = new Practitioner();

        if (StringUtils.isNotBlank(gmcCode)) {
            gmcCode = StringUtils.deleteWhitespace(gmcCode).toUpperCase();

            if (gmcCode.startsWith("G")) {
                practitioner.addIdentifier(new Identifier()
                        .setSystem(FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER)
                        .setValue(gmcCode));
            }
        }

        practitioner.setName(NameConverter.createUsualName(surname, forenames, null));

        mapAndSetId(getUniqueIdentifyingString(practitioner), practitioner);

        targetResources.addResource(practitioner);

        if (primaryCareOrganizationReference != null)
            practitioner.addPractitionerRole(new Practitioner.PractitionerPractitionerRoleComponent().setManagingOrganization(primaryCareOrganizationReference));

        return ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId());
    }

    // this method removes duplicates based on title, surname, forename, and merges the identifiers
    public List<Reference> transformAndGetReferences(List<Xcn> source) throws TransformException, MapperException, ParseException {
        Collection<List<Xcn>> practitionerGroups = source
                .stream()
                .collect(Collectors.groupingBy(t -> t.getPrefix() + t.getFamilyName() + t.getGivenName()))
                .values();

        List<Reference> references = new ArrayList<>();

        for (List<Xcn> practitioners : practitionerGroups) {
            Practitioner practitioner = createPractitionerFromDuplicates(practitioners);

            targetResources.addResource(practitioner);

            references.add(ReferenceHelper.createReference(ResourceType.Practitioner, practitioner.getId()));
        }

        return references;
    }

    private Practitioner createPractitionerFromDuplicates(List<Xcn> sources) throws TransformException, MapperException, ParseException {
        Validate.notNull(sources);

        if (sources.size() == 0)
            return null;

        Practitioner practitioner = new Practitioner();
        practitioner.setName(NameConverter.convert(sources.get(0)));

        for (Xcn source : sources) {

            Identifier identifier = IdentifierConverter.createIdentifier(source, getResourceType());

            if (identifier != null)
                if (!hasIdentifierWithSystem(practitioner.getIdentifier(), identifier.getSystem()))
                    practitioner.addIdentifier(identifier);
        }

        mapAndSetId(getUniqueIdentifyingString(practitioner), practitioner);

        return practitioner;
    }

    private boolean hasIdentifierWithSystem(List<Identifier> identifiers, String system) {
        return (getIdentifierValue(identifiers, system) != null);
    }

    private String getIdentifierValue(List<Identifier> identifiers, String system) {
        return identifiers
                .stream()
                .filter(t -> t.getSystem().equals(system))
                .map(t -> t.getValue())
                .collect(StreamExtension.firstOrNullCollector());
    }

    private String getUniqueIdentifyingString(Practitioner source) throws MapperException, TransformException {

        String forename = StringUtils.defaultString(source.getName().getGiven().get(0).getValue());
        String surname = StringUtils.defaultString(source.getName().getFamily().get(0).getValue());
        String primaryIdentifier = StringUtils.defaultString(getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_HOMERTON_PRIMARY_PRACTITIONER_ID));
        String consultantCode = StringUtils.defaultString(getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_CONSULTANT_CODE));
        String gmcCode = StringUtils.defaultString(getIdentifierValue(source.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER));

        if (StringUtils.isBlank(surname))
            throw new TransformException("surname is blank");

        return createIdentifyingString(ImmutableMap.of(
                "Surname", surname,
                "Forename", forename,
                "HomertonPersonnelPrimaryIdentifier", primaryIdentifier,
                "ConsultantCode", consultantCode,
                "GmcCode", gmcCode
        ));
    }
}
