package org.endeavourhealth.hl7parser.messages;

import java.util.Arrays;
import java.util.List;

public abstract class AdtMessageType {

    public static String AdtA01 = "ADT^A01";
    public static String AdtA02 = "ADT^A02";
    public static String AdtA03 = "ADT^A03";
    public static String AdtA04 = "ADT^A04";
    public static String AdtA05 = "ADT^A05";
    public static String AdtA06 = "ADT^A06";
    public static String AdtA07 = "ADT^A07";
    public static String AdtA08 = "ADT^A08";


    public static String AdtA17 = "ADT^A17";
    public static String AdtA34 = "ADT^A34";
    public static String AdtA35 = "ADT^A35";
    public static String AdtA44 = "ADT^A44";


    public static String A01AdmitVisitNotification = AdtA01;
    public static String A02TransferPatient = AdtA02;
    public static String A03DischargeEndVisit = AdtA03;
    public static String A04RegisterPatient = AdtA04;
    public static String A05PreAdmitPatient = AdtA05;
    public static String A06ChangeInpatientToOutpatient = AdtA06;
    public static String A07ChangeOutpatientToInpatient = AdtA07;
    public static String A08UpdatePatientInformation = AdtA08;

    public static String A17SwapPatients = AdtA17;

    public static String A34MergePatientInformationPatientId = AdtA34;
    public static String A35MergePatientInformationAccountNumber = AdtA35;
    public static String A44MoveAccountInformation = AdtA44;

    public static List<String> MergeMessages = Arrays.asList(
            A34MergePatientInformationPatientId,
            A35MergePatientInformationAccountNumber,
            A44MoveAccountInformation);
}
