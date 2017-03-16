package org.endeavourhealth.hl7transform.profiles.homerton.segments;

import org.endeavourhealth.hl7parser.*;
import org.endeavourhealth.hl7parser.datatypes.*;
import org.endeavourhealth.hl7parser.Hl7DateTime;

import java.util.List;

public class ZpiSegment extends Segment {
    public ZpiSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    //Segment cannot repeat
    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public List<Xad> getPatientTemporaryAddress() { return this.getFieldAsDatatypes(2, Xad.class); }
    public String getBirthPlace() { return this.getFieldAsString(4); }
    public Hl7DateTime getConceptionDate() throws ParseException { return this.getFieldAsHl7Date(6); }
    public Hl7DateTime getDeathDate() throws ParseException { return this.getFieldAsHl7Date(7); }
    public List<Xcn> getFamilyDoctor() { return this.getFieldAsDatatypes(8, Xcn.class); }
    public String getPatientConfidentiality() { return this.getFieldAsString(9); }
    public List<Xcn> getOtherProvider() { return this.getFieldAsDatatypes(10, Xcn.class); }
    public String getCauseOfDeath() { return this.getFieldAsString(11); }
    public String getAdoptedIndicator() { return this.getFieldAsString(12); }
    public String getChurch() { return this.getFieldAsString(13); }
    public String getAutopsyIndicator() { return this.getFieldAsString(14); }
    public String getSpecies() { return this.getFieldAsString(15); }
    public List<Ce> getDiseaseAlertCode() { return this.getFieldAsDatatypes(20, Ce.class); }
    public List<Ce> getProcessAlertCode() { return this.getFieldAsDatatypes(21, Ce.class); }
    public String getPersonVIPIndicator() { return this.getFieldAsString(22); }
}
