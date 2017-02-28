package org.endeavourhealth.hl7test;

import org.endeavourhealth.transform.hl7v2.Hl7v2Transform;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class BulkTransformTester {
    public static void processStoredMessages() throws Exception {
        System.out.println("Starting transform Process");
        System.out.println("Getting messages to transform");
        List<Integer> messageList = getMessages();
        System.out.println("List of messages obtained : " + messageList.size() + " messages");
        processMessages(messageList);

    }

    private static void processMessages(List<Integer> messageList) throws Exception {
        Integer messageCount = messageList.size();
        Integer success = 0;
        Integer fail = 0;
        Integer i = 1;
        for (Integer id: messageList) {
            String messageText = getMessage(id);
            String error = null;
            String fhirMessage = null;
            try {
                fhirMessage = transformMessage(messageText);
                success++;
            }
            catch (Exception e) {
                error = e.getMessage();
                fail++;
            }
            saveTransformResult(id, messageText, fhirMessage, error );

            Integer percentage = Math.round(i * 100 / messageCount);
            if (percentage % 10 == 0 ) { //10% increments
                System.out.println(percentage + "% Complete");
                System.out.println(success + " Successful transforms");
                System.out.println(fail + " Failed transforms");
            }
            i++;
        }
    }

    private static void saveTransformResult(Integer messageId, String hl7Message, String fhirMessage, String errorMessage) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = null;

        try
        {
            ps = conn.prepareStatement("INSERT INTO log.test_transform (message_id, hl7_payload, fhir_payload, error_message) values (?, ?, ?, ?);");
            ps.setInt(1, messageId);
            ps.setString(2, hl7Message);
            ps.setString(3, fhirMessage);
            ps.setString(4, errorMessage);
            // Get data from Oracle Database
            ps.executeUpdate();
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
            conn.close();
        }
    }

    private static String transformMessage(String message) throws Exception {
        return Hl7v2Transform.transform(message);
    }

    private static String getMessage(Integer message_id) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        String inbound_message = "";

        try
        {
            ps = conn.prepareStatement("select inbound_payload from log.message where message_id = ?;");
            ps.setInt(1, message_id);
            // Get data from Oracle Database
            ResultSet result = ps.executeQuery();
            while (result.next())
            {
                inbound_message = result.getString("inbound_payload");
            }
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
            conn.close();
        }
        return inbound_message;
    }

    private static List<Integer> getMessages() throws Exception{

        List<Integer> list = new ArrayList<>();

        Connection conn = getConnection();
        PreparedStatement ps = null;

        try
        {
            ps = conn.prepareStatement("select message_id from log.message;");

//            ps.setString(1, user);
            // Get data from Oracle Database
            ResultSet result = ps.executeQuery();
            while (result.next())
            {
                list.add(result.getInt("message_id"));
            }
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
            conn.close();
        }

        return list;
    }

    private static Connection getConnection(){
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        String url = "jdbc:postgresql://localhost:5432/hl7receiver";
        String user = "postgres";
        String pass = "admin";
        Connection db = null;
        try {
            db = DriverManager.getConnection(url,user,pass);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        return db;
    }
}
