package org.endeavourhealth.hl7receiver.mapping;

import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.hl7receiver.PostgresDataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbCode;
import org.endeavourhealth.hl7receiver.model.db.DbOrganisation;
import org.endeavourhealth.hl7receiver.model.db.DbResourceUuidMapping;
import org.endeavourhealth.hl7transform.mapper.code.MappedCode;
import org.endeavourhealth.hl7transform.mapper.code.MappedCodeAction;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.endeavourhealth.hl7transform.mapper.resource.MappedResourceUuid;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Mapper extends org.endeavourhealth.hl7transform.mapper.Mapper {
    private static final Logger LOG = LoggerFactory.getLogger(Mapper.class);

    private String sendingFacility;
    private PostgresDataLayer dataLayer;
    private CodeCache codeCache;
    private ResourceUuidCache resourceUuidCache;
    private OrganisationCache organisationCache;

    public Mapper(String sendingFacility, PostgresDataLayer dataLayer) {
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
    public List<MappedResourceUuid> getScopedResourceUuidMappings(String uniqueIdentifierPrefix) throws MapperException {
        try {
            long msStart = System.currentTimeMillis();
            List<DbResourceUuidMapping> resourceUuidMappings = this.dataLayer.getSimilarResourceUuidMappings(this.sendingFacility, uniqueIdentifierPrefix);
            long msEnd = System.currentTimeMillis();
            long msTaken = msEnd - msStart;
            LOG.trace("Took " + msTaken + "ms to get similar mappings to " + uniqueIdentifierPrefix);

            return resourceUuidMappings
                    .stream()
                    .map(t -> new MappedResourceUuid()
                            .setResourceType(t.getResourceType())
                            .setUniqueIdentifier(t.getUniqueIdentifier())
                            .setResourceUuid(t.getResourceUuid()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new MapperException("Exception while getting similar resource UUID mappings, see cause", e);
        }
    }

    @Override
    public MappedOrganisation mapOrganisation(String odsCode) throws MapperException {
        try {
            MappedOrganisation mappedOrganisation = organisationCache.getMappedOrganisation(odsCode);

            if (mappedOrganisation == null) {
                DbOrganisation dbOrganisation = this.dataLayer.getOrganisation(odsCode);

                if (dbOrganisation != null) {
                    mappedOrganisation = new MappedOrganisation()
                            .setOdsCode(dbOrganisation.getOdsCode())
                            .setOrganisationName(dbOrganisation.getOrganisationName())
                            .setOrganisationClass(dbOrganisation.getOrganisationClass())
                            .setOrganisationType(dbOrganisation.getOrganisationType())
                            .setAddressLine1(dbOrganisation.getAddressLine1())
                            .setAddressLine2(dbOrganisation.getAddressLine2())
                            .setTown(dbOrganisation.getTown())
                            .setCounty(dbOrganisation.getCounty())
                            .setPostcode(dbOrganisation.getPostcode());
                } else {

                    //the Open ODS code has now moved to the FHIR rep/*mappedOrganisation = OdsRestClient.lookupOrganisationViaRest(odsCode);
                    OdsOrganisation odsOrg = OdsWebService.lookupOrganisationViaRest(odsCode);
                    if (odsOrg == null) {
                        return null;
                    }

                    //new version of ODS API returns multiple types, so attempt to get that down to ONE
                    OrganisationType organisationType = null;

                    Set<OrganisationType> types = new HashSet<>(odsOrg.getOrganisationTypes());
                    types.remove(OrganisationType.PRESCRIBING_COST_CENTRE); //always remove so we match to the "better" type
                    if (types.size() == 1) {
                        organisationType = types.iterator().next();
                    } else {
                        LOG.warn("Could not select type for org " + odsOrg);
                    }

                    //add to our local DB for next time
                    this.dataLayer.setOrganisation(odsOrg.getOdsCode(),
                            odsOrg.getOrganisationName(),
                            odsOrg.getOrganisationClass(),
                            organisationType,
                            odsOrg.getAddressLine1(),
                            odsOrg.getAddressLine2(),
                            odsOrg.getTown(),
                            odsOrg.getCounty(),
                            odsOrg.getPostcode());

                    mappedOrganisation = new MappedOrganisation()
                            .setOdsCode(odsOrg.getOdsCode())
                            .setOrganisationName(odsOrg.getOrganisationName())
                            .setOrganisationClass(odsOrg.getOrganisationClass())
                            .setOrganisationType(organisationType)
                            .setAddressLine1(odsOrg.getAddressLine1())
                            .setAddressLine2(odsOrg.getAddressLine2())
                            .setTown(odsOrg.getTown())
                            .setCounty(odsOrg.getCounty())
                            .setPostcode(odsOrg.getPostcode());

                }

                organisationCache.putMappedOrganisation(odsCode, mappedOrganisation);
            }

            return mappedOrganisation;

        } catch (Exception e) {
            throw new MapperException("Exception while mapping organisation, see cause", e);
        }
    }
}
