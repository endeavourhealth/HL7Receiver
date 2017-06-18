package org.endeavourhealth.hl7receiver.mapping;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.schema.OrganisationClass;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.hl7transform.common.converters.AddressConverter;
import org.endeavourhealth.hl7transform.common.converters.StringHelper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.organisation.MappedOrganisation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class OdsRestClient {

    // needs rewiring to Stu's ODS REST lookup, using openods.co.uk for now
    private static final String ORGANISATAION_REST_URL = "http://test.openods.co.uk/api/organisations/";

    public static MappedOrganisation lookupOrganisationViaRest(String odsCode) throws MapperException, UnirestException {
        Validate.notEmpty(odsCode);

        HttpResponse<JsonNode> response = Unirest.get(ORGANISATAION_REST_URL + odsCode).asJson();

        if (response.getStatus() == 404)
            return null;

        if (response.getStatus() != 200)
            throw new MapperException("Status " + Integer.toString(response.getStatus()) + " returned when getting organisation " + odsCode + " from " + ORGANISATAION_REST_URL + odsCode);

        JSONObject organisation = response.getBody().getObject();

        MappedOrganisation mappedOrganisation = new MappedOrganisation()
                .setOrganisationName(StringHelper.formatName(organisation.getString("name")))
                .setOdsCode(organisation.getString("odsCode"))
                .setOrganisationClass(OrganisationClass.fromOrganisationClassName(organisation.getString("recordClass")));

        if (StringUtils.isEmpty(mappedOrganisation.getOdsCode()))
            throw new MapperException("Returned ODS code is empty");

        if (StringUtils.isEmpty(mappedOrganisation.getOrganisationName()))
            throw new MapperException("Returned organisation name is empty");

        JSONObject address = getFirstJSONObject(getArray(organisation, "addresses"));

        if (address != null) {
            JSONArray addressLines = getArray(address, "addressLines");

            if (addressLines != null) {
                if (addressLines.length() > 0)
                    mappedOrganisation.setAddressLine1(AddressConverter.formatAddressLine(addressLines.getString(0)));

                if (addressLines.length() > 1) {
                    String line2 = AddressConverter.formatAddressLine(addressLines.getString(1));

                    if (addressLines.length() > 2)
                        line2 = line2 + ", " + AddressConverter.formatAddressLine(addressLines.getString(2));

                    mappedOrganisation.setAddressLine2(line2);
                }
            }

            mappedOrganisation.setTown(AddressConverter.formatAddressLine(address.getString("town")));
            mappedOrganisation.setCounty(AddressConverter.formatAddressLine(address.getString("county")));
            mappedOrganisation.setPostcode(AddressConverter.formatPostcode(address.getString("postCode")));
        }

        JSONArray roles = getArray(organisation, "roles");
        OrganisationType organisationType = null;

        if (roles != null) {
            String primaryRole = null;

            List<String> otherRoles = new ArrayList<>();

            for (int i = 0; i < roles.length(); i++) {
                JSONObject role = roles.getJSONObject(i);

                if (role.getBoolean("primaryRole"))
                    primaryRole = role.getString("code");
                else
                    otherRoles.add(role.getString("code"));
            }

            organisationType = getOrganisationType(primaryRole);

            for (String roleCode : otherRoles) {
                if (organisationType != null)
                    break;

                organisationType = getOrganisationType(roleCode);
            }
        }

        if (organisationType == null)
            throw new MapperException("Could not determine organisation type when getting organisation " + odsCode + " from " + ORGANISATAION_REST_URL + odsCode);

        mappedOrganisation.setOrganisationType(organisationType);

        return mappedOrganisation;
    }

    public static JSONArray getArray(JSONObject jsonObject, String key) {
        if (jsonObject == null)
            return null;

        if (jsonObject.has(key))
            return jsonObject.getJSONArray(key);

        return null;
    }

    public static JSONObject getFirstJSONObject(JSONArray jsonArray) {
        if (jsonArray == null)
            return null;

        if (jsonArray.length() == 0)
            return null;

        return jsonArray.getJSONObject(0);
    }

    private static OrganisationType getOrganisationType(String roleCode) {
        try {
            return OrganisationType.fromRoleCode(roleCode);
        } catch (Exception e) {
            return null;
        }
    }
}
