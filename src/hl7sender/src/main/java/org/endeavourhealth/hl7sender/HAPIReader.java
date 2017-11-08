package org.endeavourhealth.hl7sender;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator;
import ca.uhn.hl7v2.util.Terser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HAPIReader extends AbstractMessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(HAPIReader.class);
    private Hl7InputStreamMessageIterator hapiIter;

    public void prepare() throws FileNotFoundException {
        FileReader reader = new FileReader(getInputMessages());
        hapiIter = new Hl7InputStreamMessageIterator(reader);
    }

    @Override
    public void prepareRestart() throws IOException {
        LOG.info("Looking for last successful send in audit log.....");
        FileInputStream fstream = new FileInputStream(auditFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        while ((strLine = br.readLine()) != null)   {
            if (strLine.startsWith(Main.SUCCESS_LINE)) {
                lastSuccessSendMsgId = strLine.split(":")[1];
                skipMessages = true;
            }
        }
        br.close();
        fstream.close();
        LOG.info("..... Found:" + lastSuccessSendMsgId);
    }

    @Override
    public Message next() throws HL7Exception {
        Message ret = null;

        while (hapiIter.hasNext() && ret == null) {
            Message nextMsg = hapiIter.next();
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
        hapiIter = null;
    }

}
