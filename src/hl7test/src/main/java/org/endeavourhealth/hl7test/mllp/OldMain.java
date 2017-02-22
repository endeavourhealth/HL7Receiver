package org.endeavourhealth.hl7test.mllp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class OldMain {

    private static String ipAddress = "127.0.0.1";
    private static int port = 8900;

    private static String msg =
            "MSH|^~\\&|NES|NINTENDO|TESTSYSTEM|TESTFACILITY|20010101000000||ADT^A04|Q123456789T123456789X123456|P|2.3\r" +
            "EVN|A04|20010101000000|||^KOOPA^BOWSER^^^^^^^CURRENT\r" +
            "PID|1||123456789|0123456789^AA^^JP|BROS^MARIO^^^^||19850101000000|M|||123 FAKE STREET^MARIO \\T\\ LUIGI BROS PLACE^TOADSTOOL KINGDOM^NES^A1B2C3^JP^HOME^^1234|1234|(555)555-0123^HOME^JP:1234567|||S|MSH|12345678|||||||0|||||N\r" +
            "NK1|1|PEACH^PRINCESS^^^^|SO|ANOTHER CASTLE^^TOADSTOOL KINGDOM^NES^^JP|(123)555-1234|(123)555-2345|NOK|||||||||||||\r" +
            "NK1|2|TOADSTOOL^PRINCESS^^^^|SO|YET ANOTHER CASTLE^^TOADSTOOL KINGDOM^NES^^JP|(123)555-3456|(123)555-4567|EMC|||||||||||||\r" +
            "PV1|1|O|ABCD^EFGH^|||^^|123456^DINO^YOSHI^^^^^^MSRM^CURRENT^^^NEIGHBOURHOOD DR NBR^|^DOG^DUCKHUNT^^^^^^^CURRENT||CRD|||||||123456^DINO^YOSHI^^^^^^MSRM^CURRENT^^^NEIGHBOURHOOD DR NBR^|AO|0123456789|1|||||||||||||||||||MSH||A|||20010101000000\r" +
            "IN1|1|PAR^PARENT||||LUIGI\r" +
            "IN1|2|FRI^FRIEND||||PRINCESS";

    private static int mllpStart = 0x0b;
    private static int mllpEnd = 0x1c;
    private static int mllpEnd2 = 0x0d;

    public static void main(String[] args) {

        try {
            Socket socket = new Socket(ipAddress, port);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            byte[] message = getPaddedMessage(msg);

            out.write(message);
            out.write(message);

            BufferedReader bis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine;
            while ((inputLine = bis.readLine()) != null)
                System.out.println(inputLine);

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getPaddedMessage(String message) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(mllpStart);
        outputStream.write(message.getBytes(StandardCharsets.US_ASCII));
        outputStream.write(mllpEnd);
        outputStream.write(mllpEnd2);
        return outputStream.toByteArray();
    }
}
