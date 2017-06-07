package org.endeavourhealth.hl7transform.transforms.homerton.parser.zdatatypes;

import org.endeavourhealth.hl7parser.Datatype;
import org.endeavourhealth.hl7parser.GenericDatatype;

public class Zpd extends Datatype {
    public Zpd(GenericDatatype datatype) {
        super(datatype);
    }

     /*
        Homerton specific

        PD1.4 contains both primary care organisation and doctor
        Normally it only contains the doctor.

        Homerton's PD1.4:

        1          2       3         5           6       7            8        9.1      9.2      9.3          14.1       14.2
        DoctorCode^Surname^Forename^^PhoneNumber^OdsCode^PracticeName^Address1^Address2&Address3&Address4^^^^^PctOdsCode&ShaOdsCode

        Examples:

        G3339344^JONES^A^^1937573848^B86010^DR SR LIGHTFOOT & PARTNERS^Church View Surgery^School Lane&&LS22 5BQ^^^^^Q12&5HJ
        G3426512^SMITH^ROBERT^^020 89867111^F84003^LOWER CLAPTON GROUP PRACTICE^Lower Clapton Health Ctr.^36 Lower Clapton Road&London&E5 0PD^^^^^Q06&5C3
     */

    public String getGmcCode() { return this.getComponentAsString(1); }
    public String getSurname() { return this.getComponentAsString(2); }
    public String getForenames() { return this.getComponentAsString(3); }
    public String getPhoneNumber() { return this.getComponentAsString(5); }
    public String getOdsCode() { return this.getComponentAsString(6); }
    public String getPracticeName() { return this.getComponentAsString(7); }
    public String getAddressLine1() { return this.getComponentAsString(8); }
    public String getAddressLine2() { return this.getSubcomponentAsString(9, 1); }
    public String getTown() { return this.getSubcomponentAsString(9, 2); }
    public String getPostcode() { return this.getSubcomponentAsString(9, 3); };
    public String getPctOdsCode() { return this.getSubcomponentAsString(14, 1); }
    public String getShaOdsCode() { return this.getSubcomponentAsString(14, 2); }
}
