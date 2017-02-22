package org.endeavourhealth.hl7receiver.model.db;

public class DbEds {
    private String edsUrl;
    private String softwareContentType;
    private String softwareVersion;
    private boolean useKeycloak;
    private String keycloakTokenUri;
    private String keycloakRealm;
    private String keycloakUsername;
    private String keycloakPassword;
    private String keycloakClientId;

    public String getEdsUrl() {
        return edsUrl;
    }

    public DbEds setEdsUrl(String edsUrl) {
        this.edsUrl = edsUrl;
        return this;
    }

    public String getSoftwareContentType() {
        return softwareContentType;
    }

    public DbEds setSoftwareContentType(String softwareContentType) {
        this.softwareContentType = softwareContentType;
        return this;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public DbEds setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
        return this;
    }

    public boolean isUseKeycloak() {
        return useKeycloak;
    }

    public DbEds setUseKeycloak(boolean useKeycloak) {
        this.useKeycloak = useKeycloak;
        return this;
    }

    public String getKeycloakTokenUri() {
        return keycloakTokenUri;
    }

    public DbEds setKeycloakTokenUri(String keycloakTokenUri) {
        this.keycloakTokenUri = keycloakTokenUri;
        return this;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public DbEds setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
        return this;
    }

    public String getKeycloakUsername() {
        return keycloakUsername;
    }

    public DbEds setKeycloakUsername(String keycloakUsername) {
        this.keycloakUsername = keycloakUsername;
        return this;
    }

    public String getKeycloakPassword() {
        return keycloakPassword;
    }

    public DbEds setKeycloakPassword(String keycloakPassword) {
        this.keycloakPassword = keycloakPassword;
        return this;
    }

    public String getKeycloakClientId() {
        return keycloakClientId;
    }

    public DbEds setKeycloakClientId(String keycloakClientId) {
        this.keycloakClientId = keycloakClientId;
        return this;
    }
}
