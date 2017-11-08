package org.endeavourhealth.hl7sender;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class AbstractMessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMessageReader.class);
    private File inputMessages = null;
    private File archiveDir = null;
    protected File auditFile = null;
    protected String lastSuccessSendMsgId = null;
    protected boolean skipMessages = false;

    public abstract void prepare() throws FileNotFoundException, IOException, HL7Exception;
    public abstract void prepareRestart() throws IOException;
    public abstract Message next() throws IOException, HL7Exception, FileNotFoundException;
    public abstract void postSend() throws IOException;
    public abstract void close() throws IOException;

    public File getInputMessages() {
        return inputMessages;
    }

    public void setInputMessages(File inputMessages) {
        this.inputMessages = inputMessages;
    }

    public void setArchiveDir(File archiveDir) {
        this.archiveDir = archiveDir;
        LOG.debug("Setting archive dir::" + archiveDir.getAbsolutePath());
    }

    public File getArchiveDir() {
        return archiveDir;
    }

    public void setAuditFile(File auditFile) {
        this.auditFile = auditFile;
    }
}
