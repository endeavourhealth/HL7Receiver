package org.endeavourhealth.hl7transform.homerton.transforms.valuesets;

import org.apache.commons.lang3.NotImplementedException;

public abstract class MessageTypeVs {

    public static String getDescription(String messageType) {

        switch (messageType) {
            case "ADT^A01": return "Admit / visit notification";
            case "ADT^A02": return "Transfer a patient";
            case "ADT^A03": return "Discharge/end visit";
            case "ADT^A04": return "Register a patient";
            case "ADT^A05": return "Pre-admit a patient";
            case "ADT^A06": return "Change an outpatient to an inpatient";
            case "ADT^A07": return "Change an inpatient to an outpatient";
            case "ADT^A08": return "Update patient information";
            case "ADT^A09": return "Patient departing - tracking";
            case "ADT^A10": return "Patient arriving - tracking";
            case "ADT^A11": return "Cancel admit/visit notification";
            case "ADT^A12": return "Cancel transfer";
            case "ADT^A13": return "Cancel discharge/end visit";
            case "ADT^A14": return "Pending admit";
            case "ADT^A15": return "Pending transfer";
            case "ADT^A16": return "Pending discharge";
            case "ADT^A17": return "Swap patients";
            case "ADT^A18": return "Merge patient information";
            case "ADT^A19": return "Patient query";
            case "ADT^A20": return "Bed status update";
            case "ADT^A21": return "Patient goes on a leave of absence";
            case "ADT^A22": return "Patient returns from a leave of absence";
            case "ADT^A23": return "Delete a patient record";
            case "ADT^A24": return "Link patient information";
            case "ADT^A25": return "Cancel pending discharge";
            case "ADT^A26": return "Cancel pending transfer";
            case "ADT^A27": return "Cancel pending admit";
            case "ADT^A28": return "Add person information";
            case "ADT^A29": return "Delete person information";
            case "ADT^A30": return "Merge person information";
            case "ADT^A31": return "Update person information";
            case "ADT^A32": return "Cancel patient arriving - tracking";
            case "ADT^A33": return "Cancel patient departing - tracking";
            case "ADT^A34": return "Merge patient information - patient ID only";
            case "ADT^A35": return "Merge patient information - account number only";
            case "ADT^A36": return "Merge patient information - patient ID and account number";
            case "ADT^A37": return "Unlink patient information";
            case "ADT^A38": return "Cancel pre-admit";
            case "ADT^A39": return "Merge person - external ID";
            case "ADT^A40": return "Merge patient - internal ID";
            case "ADT^A41": return "Merge account - patient account number";
            case "ADT^A42": return "Merge visit - visit number";
            case "ADT^A43": return "Move patient information - internal ID";
            case "ADT^A44": return "Move account information - patient account number";
            case "ADT^A45": return "Move visit information - visit number";
            case "ADT^A46": return "Change external ID";
            case "ADT^A47": return "Change internal ID";
            case "ADT^A48": return "Change alternate patient ID";
            case "ADT^A49": return "Change patient account number";
            case "ADT^A50": return "Change visit number";
            case "ADT^A51": return "Change alternate visit ID";

            default: throw new NotImplementedException(messageType + " message type not recognised");
        }
    }
}
