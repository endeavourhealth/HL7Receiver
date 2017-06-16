package org.endeavourhealth.hl7receiver.model.db;

import org.endeavourhealth.common.fhir.schema.OrganisationType;

public class DbOrganisation {
    private String odsCode;
    private String organisationName;
    private OrganisationType organisationType;
    private String addressLine1;
    private String addressLine2;
    private String town;
    private String county;
    private String postcode;
    private String phoneNumber;
    private boolean isMapped;

    public String getOdsCode() {
        return odsCode;
    }

    public DbOrganisation setOdsCode(String odsCode) {
        this.odsCode = odsCode;
        return this;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public DbOrganisation setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
        return this;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public DbOrganisation setOrganisationType(OrganisationType organisationType) {
        this.organisationType = organisationType;
        return this;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public DbOrganisation setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return this;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public DbOrganisation setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return this;
    }

    public String getTown() {
        return town;
    }

    public DbOrganisation setTown(String town) {
        this.town = town;
        return this;
    }

    public String getCounty() {
        return county;
    }

    public DbOrganisation setCounty(String county) {
        this.county = county;
        return this;
    }

    public String getPostcode() {
        return postcode;
    }

    public DbOrganisation setPostcode(String postcode) {
        this.postcode = postcode;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public DbOrganisation setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public boolean isMapped() {
        return isMapped;
    }

    public DbOrganisation setMapped(boolean mapped) {
        isMapped = mapped;
        return this;
    }
}
