package org.endeavourhealth.hl7sender;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class PSQLReader extends AbstractMessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(PSQLReader.class);
    static BufferedReader psqlReader;
    static Message psqlReaderNextmsg = null;

    public void prepare() throws FileNotFoundException, IOException, HL7Exception {
        psqlReader = new BufferedReader(new FileReader(getInputMessages()));
        readNextPsqlMessage();
    }

    @Override
    public void prepareRestart() {

    }

    @Override
    public Message next() throws IOException, HL7Exception {
        Message ret = null;

        while (psqlReaderNextmsg != null && ret == null) {
            Message nextMsg = psqlReaderNextmsg;
            readNextPsqlMessage();

            if (skipMessages) {
                Terser nextMsgTerser = new Terser(nextMsg);
                String currMSH10 = nextMsgTerser.get("/MSH-10");
                if (lastSuccessSendMsgId.compareTo(currMSH10) == 0) {
                    LOG.info("Skip message (for the last time):" + currMSH10);
                    skipMessages = false;
                } else {
                    LOG.info("Skip message:" + currMSH10);
                }
            } else {
                ret = nextMsg;
            }
        }

        return ret;
    }

    public void postSend() {

    }

    @Override
    public void close() throws IOException {
        psqlReader.close();
    }

    private void readNextPsqlMessage() throws IOException, HL7Exception {
        psqlReaderNextmsg = null;
        String strLine;
        while ((strLine = psqlReader.readLine()) != null)   {
            //LOG.info("read line=>" + strLine);
            if (strLine.trim().startsWith("MSH")) {
                //LOG.info("Found msg=>" + strLine);
                String adjustedMsg = strLine.trim().replaceAll("\\\\r","\r\n");
                DefaultHapiContext ctx = new DefaultHapiContext();
                PipeParser pp = ctx.getPipeParser();
                pp.setValidationContext(new NoValidation());
                psqlReaderNextmsg = pp.parse(adjustedMsg);
            }
            if (psqlReaderNextmsg != null) {
                return;
            }
        }
    }

}
