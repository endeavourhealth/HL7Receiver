package org.endeavourhealth.hl7receiver.engine;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.*;
import org.endeavourhealth.hl7parser.Message;
import org.endeavourhealth.hl7parser.datatypes.Cx;
import org.endeavourhealth.hl7parser.segments.MshSegment;
import org.endeavourhealth.hl7parser.segments.PidSegment;
import org.endeavourhealth.hl7parser.segments.SegmentName;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;

import java.time.LocalDateTime;
import java.util.List;

class HL7KeyFields2 {

    private String encodedMessage;
    private String sendingApplication;
    private String sendingFacility;
    private String receivingApplication;
    private String receivingFacility;
    private LocalDateTime messageDateTime;
    private String messageType;
    private String messageControlId;
    private String sequenceNumber;
    private String pid1;
    private String pid2;

    public static HL7KeyFields2 parse(ca.uhn.hl7v2.model.Message hapiMessage, DbChannel channel) throws ParseException, HL7Exception {

        String messageText = new DefaultHapiContext().getPipeParser().encode(hapiMessage);

        HL7KeyFields2 hl7KeyFields = new HL7KeyFields2();
        hl7KeyFields.encodedMessage = messageText;

        Message message = new Message(messageText);

        MshSegment mshSegment = message.getSegment(SegmentName.MSH, MshSegment.class);

        if (mshSegment != null) {
            hl7KeyFields.sendingApplication = mshSegment.getSendingApplication();
            hl7KeyFields.sendingFacility = mshSegment.getSendingFacility();
            hl7KeyFields.receivingApplication = mshSegment.getReceivingApplication();
            hl7KeyFields.receivingFacility = mshSegment.getReceivingFacility();
            hl7KeyFields.messageDateTime = getLocalDateTime(mshSegment.getDateTimeOfMessage());
            hl7KeyFields.messageType = mshSegment.getMessageType();
            hl7KeyFields.messageControlId = mshSegment.getMessageControlId();
            hl7KeyFields.sequenceNumber = getIntegerAsString(mshSegment.getSequenceNumber());
        }

        PidSegment pidSegment = message.getSegment(SegmentName.PID, PidSegment.class);

        if (pidSegment != null) {
            hl7KeyFields.pid1 = formatPid(getPid2(pidSegment, channel.getPid1Field(), channel.getPid1AssigningAuthority()));
            hl7KeyFields.pid2 = formatPid(getPid2(pidSegment, channel.getPid2Field(), channel.getPid2AssigningAuthority()));
        }

        return hl7KeyFields;
    }

    private static String getPid2(PidSegment pidSegment, Integer pidFieldNumber, String assigningAuthority) {
        List<Cx> cxs = pidSegment.getFieldAsDatatypes(pidFieldNumber, Cx.class);

        if (cxs == null)
            return null;

        for (Cx cx : cxs) {

            if (cx == null)
                continue;

            if ((StringUtils.isBlank(assigningAuthority)) && (StringUtils.isBlank(cx.getAssigningAuthority())))
                return cx.getId();

            if (assigningAuthority.equals(cx.getAssigningAuthority()))
                return cx.getId();
        }

        return null;

    }

    private static LocalDateTime getLocalDateTime(Hl7DateTime hl7DateTime) {
        if (hl7DateTime == null)
            return null;

        return hl7DateTime.getLocalDateTime();
    }

    private static String getIntegerAsString(Integer integer) {
        if (integer == null)
            return null;

        return integer.toString();
    }

    private static String formatPid(String pid) {
        if (pid == null)
            return null;

        return StringUtils.deleteWhitespace(pid);
    }

    public String getEncodedMessage() {
        return encodedMessage;
    }

    public String getSendingApplication() {
        return sendingApplication;
    }

    public String getSendingFacility() {
        return sendingFacility;
    }

    public String getReceivingApplication() {
        return receivingApplication;
    }

    public String getReceivingFacility() {
        return receivingFacility;
    }

    public LocalDateTime getMessageDateTime() {
        return messageDateTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessageControlId() {
        return messageControlId;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public String getPid1() {
        return pid1;
    }

    public String getPid2() {
        return pid2;
    }
}
