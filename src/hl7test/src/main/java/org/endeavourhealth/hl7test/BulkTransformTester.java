package org.endeavourhealth.hl7test;

import org.endeavourhealth.hl7transform.Hl7v2Transform;
import org.endeavourhealth.hl7transform.mapper.EmptyMapper;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class BulkTransformTester {
    private static Connection conn;

    public static void processStoredMessages(Integer limit, String mode) throws Exception {
        conn = getConnection();
        try {
            System.out.println("Starting transform Process");
            System.out.println("Getting messages to transform");
            if (limit > 0)
                System.out.println("Limited to " + limit.toString() + " messages");
            System.out.println("Mode = " + mode);

            if (mode.equals("reset")) {
                System.out.println("Resetting all progress");
                resetProgress();
            }

            List<Integer> messageList = getMessages(limit, mode);
            System.out.println("List of messages obtained : " + messageList.size() + " messages");
            processMessages(messageList);
        }
        finally {
            conn.close();
        }
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
                error = constructExceptionMessage(e);
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
            Thread.sleep(100);
        }
    }

    private static void saveTransformResult(Integer messageId, String hl7Message, String fhirMessage, String errorMessage) throws Exception {

        PreparedStatement ps = null;

        String statement = "UPDATE log.test_transform SET hl7_payload = ?, fhir_payload = ?, error_message= ? WHERE message_id=?;";
        statement += " INSERT INTO log.test_transform (message_id, hl7_payload, fhir_payload, error_message)";
        statement += " select ?, ?, ?, ?";
        statement += " WHERE NOT EXISTS (SELECT message_id FROM log.test_transform WHERE message_id=?);";
        try
        {
            ps = conn.prepareStatement(statement);
            ps.setString(1, hl7Message);
            ps.setString(2, fhirMessage);
            ps.setString(3, errorMessage);
            ps.setInt(4, messageId);
            ps.setInt(5, messageId);
            ps.setString(6, hl7Message);
            ps.setString(7, fhirMessage);
            ps.setString(8, errorMessage);
            ps.setInt(9, messageId);
            ps.executeUpdate();
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
    }

    private static void resetProgress() throws Exception {

        PreparedStatement ps = null;

        try
        {
            ps = conn.prepareStatement("truncate table log.test_transform;");

            // Get data from Oracle Database
            ps.executeUpdate();
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
    }

    private static String transformMessage(String message) throws Exception {
        return Hl7v2Transform.transform(message, new EmptyMapper());
    }

    private static String getMessage(Integer message_id) throws Exception {

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
        }
        return inbound_message;
    }

    private static List<Integer> getMessages(Integer limit, String mode) throws Exception{

        List<Integer> list = new ArrayList<>();

        PreparedStatement ps = null;
        String statement = "select m.message_id from log.message m";
        statement += " left outer join log.test_transform tt on tt.message_id = m.message_id";

        if (mode.equals("errors"))
            statement += " where tt.error_message is not null";
        else
            statement += " where tt.message_id is null";
        statement += " order by message_id";

        if (limit > 0) {
            statement += " limit ?;";
        }
        else
            statement += ";";

        try
        {
            ps = conn.prepareStatement(statement);

            if (limit > 0)
                ps.setInt(1, limit);
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
        }

        return list;
    }

    private static Connection getConnection(){
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        String url = "jdbc:postgresql://" + System.getProperty("ipaddress") + ":5432/hl7receiver";
        String user = System.getProperty("user");
        String pass = System.getProperty("password");

        Connection db = null;
        try {
            db = DriverManager.getConnection(url,user,pass);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        return db;
    }

    private static String constructExceptionMessage(Throwable exception) {
        if (exception == null)
            return "";

        String message = "[" + exception.getClass().getName() + "]  " + exception.getMessage();
        message += "[Stack Trace] " + getFirstLineStackTrace(exception);

        if (exception.getCause() != null)
            if (exception.getCause() != exception)
                message += "\r\n" + constructExceptionMessage(exception.getCause());

        return message;
    }

    private static String getFirstLineStackTrace(Throwable exception) {
        if (exception.getStackTrace() != null)
            if (exception.getStackTrace().length > 0)
                return exception.getStackTrace()[0].toString();

        return "";
    }

}
