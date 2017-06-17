package org.endeavourhealth.hl7receiver.mapping;

import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbCode;
import org.endeavourhealth.hl7receiver.model.db.DbOrganisation;
import org.endeavourhealth.hl7transform.mapper.code.MappedCode;
import org.endeavourhealth.hl7transform.mapper.code.MappedCodeAction;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class Mapper extends org.endeavourhealth.hl7transform.mapper.Mapper {

    private String sendingFacility;
    private DataLayer dataLayer;
    private CodeCache codeCache;
    private ResourceUuidCache resourceUuidCache;
    private OrganisationCache organisationCache;

    public Mapper(String sendingFacility, DataLayer dataLayer) {
        this.sendingFacility = sendingFacility;
        this.dataLayer = dataLayer;
        this.codeCache = new CodeCache(MappedCodeAction.MAPPED_INCLUDE);
        this.resourceUuidCache = new ResourceUuidCache(ResourceType.Organization, ResourceType.Location, ResourceType.Practitioner);
        this.organisationCache = new OrganisationCache();
    }

    @Override
    public MappedCode mapCode(String context, String code, String codeSystem, String term) throws MapperException {
        try {
            MappedCode mappedCode = this.codeCache.getMappedCode(context, code, codeSystem, term);

            if (mappedCode == null) {

                DbCode dbCode = this.dataLayer.getCode(this.sendingFacility, context, code, codeSystem, term);

                mappedCode = new MappedCode()
                        .setTargetAction(MappedCodeAction.fromIdentifier(dbCode.getTargetAction()))
                        .setCode(dbCode.getCode())
                        .setSystem(dbCode.getSystem())
                        .setTerm(dbCode.getTerm());

                this.codeCache.putMappedCode(context, code, codeSystem, term, mappedCode);
            }

            return mappedCode;

        } catch (Exception e) {
            throw new MapperException("Exception while mapping code, see cause", e);
        }
    }

    @Override
    public UUID mapScopedResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        return this.mapResourceUuid(this.sendingFacility, resourceType, identifier);
    }

    @Override
    public UUID mapGlobalResourceUuid(ResourceType resourceType, String identifier) throws MapperException {
        return this.mapResourceUuid(Mapper.SCOPE_GLOBAL, resourceType, identifier);
    }

    private UUID mapResourceUuid(String scopeName, ResourceType resourceType, String identifier) throws MapperException {
        try {
            UUID resourceUuid = resourceUuidCache.getResourceUuid(resourceType, identifier);

            if (resourceUuid == null) {
                resourceUuid = this.dataLayer.getResourceUuid(scopeName, resourceType.toString(), identifier);
                resourceUuidCache.putResourceUuid(resourceType, identifier, resourceUuid);
            }

            return resourceUuid;

        } catch (Exception e) {
            throw new MapperException("Exception while getting resource UUID, see cause", e);
        }
    }

    @Override
    public MappedOrganisation mapOrganisation(String odsCode) throws MapperException {
        try {
            MappedOrganisation mappedOrganisation = organisationCache.getMappedOrganisation(odsCode);

            if (mappedOrganisation == null) {
                DbOrganisation dbOrganisation = this.dataLayer.getOrganisation(odsCode);

                if (dbOrganisation.isMapped()) {
                    mappedOrganisation = new MappedOrganisation()
                            .setOdsCode(dbOrganisation.getOdsCode())
                            .setOrganisationName(dbOrganisation.getOrganisationName())
                            .setOrganisationType(dbOrganisation.getOrganisationType())
                            .setAddressLine1(dbOrganisation.getAddressLine1())
                            .setAddressLine2(dbOrganisation.getAddressLine2())
                            .setTown(dbOrganisation.getTown())
                            .setCounty(dbOrganisation.getCounty())
                            .setPostcode(dbOrganisation.getPostcode());
                } else {
                    mappedOrganisation = OdsRestClient.lookupOrganisationViaRest(odsCode);

                    if (mappedOrganisation == null)
                        return null;

                    this.dataLayer.setOrganisation(mappedOrganisation.getOdsCode(),
                            mappedOrganisation.getOrganisationName(),
                            mappedOrganisation.getOrganisationType(), // need to parse type from REST service
                            mappedOrganisation.getAddressLine1(),
                            mappedOrganisation.getAddressLine2(),
                            mappedOrganisation.getTown(),
                            mappedOrganisation.getCounty(),
                            mappedOrganisation.getPostcode());
                }

                organisationCache.putMappedOrganisation(odsCode, mappedOrganisation);
            }

            return mappedOrganisation;

        } catch (Exception e) {
            throw new MapperException("Exception while mapping organisation, see cause", e);
        }
    }
}
