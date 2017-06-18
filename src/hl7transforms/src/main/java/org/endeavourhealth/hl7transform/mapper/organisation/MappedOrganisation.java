package org.endeavourhealth.hl7transform.mapper.organisation;

import org.endeavourhealth.common.fhir.schema.OrganisationClass;
import org.endeavourhealth.common.fhir.schema.OrganisationType;

public class MappedOrganisation {
    private String odsCode;
    private String organisationName;
    private OrganisationClass organisationClass;
    private OrganisationType organisationType;
    private String addressLine1;
    private String addressLine2;
    private String town;
    private String county;
    private String postcode;

    public String getOdsCode() {
        return odsCode;
    }

    public MappedOrganisation setOdsCode(String odsCode) {
        this.odsCode = odsCode;
        return this;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public MappedOrganisation setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
        return this;
    }

    public OrganisationClass getOrganisationClass() {
        return organisationClass;
    }

    public MappedOrganisation setOrganisationClass(OrganisationClass organisationClass) {
        this.organisationClass = organisationClass;
        return this;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public MappedOrganisation setOrganisationType(OrganisationType organisationType) {
        this.organisationType = organisationType;
        return this;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public MappedOrganisation setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return this;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public MappedOrganisation setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return this;
    }

    public String getTown() {
        return town;
    }

    public MappedOrganisation setTown(String town) {
        this.town = town;
        return this;
    }

    public String getCounty() {
        return county;
    }

    public MappedOrganisation setCounty(String county) {
        this.county = county;
        return this;
    }

    public String getPostcode() {
        return postcode;
    }

    public MappedOrganisation setPostcode(String postcode) {
        this.postcode = postcode;
        return this;
    }
}
