package org.endeavourhealth.hl7sender;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator;
import ca.uhn.hl7v2.util.Terser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class DirectoryReader extends AbstractMessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryReader.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private File currentFile = null;
    private File[] fileList = null;
    private int fileListPos = 0;

    /*
     *
     */
    public void prepare() throws FileNotFoundException {
        fileList = getInputMessages().listFiles();
        if (fileList != null) {
            Arrays.sort(fileList);
        }
    }

    public void prepareRestart() {
        File[] archiveFileList = getArchiveDir().listFiles();
        if (archiveFileList != null && archiveFileList.length > 0) {
            Arrays.sort(archiveFileList);
            skipMessages = true;
            lastSuccessSendMsgId = archiveFileList[archiveFileList.length - 1].getName();
            LOG.info("Skip message (last file):" + lastSuccessSendMsgId );
        }
    }

    public boolean hasNext() {
        if ((fileList != null) && (fileList.length > 0) && (fileListPos < fileList.length)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Message next() throws IOException {
        Message ret = null;

        while (hasNext() && ret == null) {
            currentFile = fileList[fileListPos];
            fileListPos++;

            if (skipMessages) {
                if (lastSuccessSendMsgId.compareTo(currentFile.getName()) == 0) {
                    LOG.info("Skip message (for the last time):" + currentFile.getName());
                    skipMessages = false;
                } else {
                    LOG.info("Skip message:" + currentFile.getName());
                }
            } else {
                FileReader reader = new FileReader(currentFile);
                Hl7InputStreamMessageIterator hapiIter = new Hl7InputStreamMessageIterator(reader);
                ret = hapiIter.next();
                reader.close();
            }
        }

        return ret;
    }

    public void postSend() throws IOException {
        //String fileAddOn = UUID.randomUUID().toString();
        //String fileAddOn = dateFormat.format(new Date()).replaceAll(":", "-");
        LOG.info("ArchiveDir:" + getArchiveDir().getAbsolutePath());
        LOG.info("CurrFile:" + currentFile.getName());
        File newFile = new File(getArchiveDir().getAbsolutePath() + File.separator + currentFile.getName());
        if (newFile.exists()) {
            LOG.info("File " + newFile.getAbsolutePath() + " already in archive");
        } else {
            LOG.info("Copying file:" + currentFile.getAbsolutePath() + " To:" + newFile.getAbsolutePath());
            Path c = Paths.get(currentFile.getAbsolutePath());
            Path n = Paths.get(newFile.getAbsolutePath());
            Files.copy(c, n, REPLACE_EXISTING);
        }
    }

    @Override
    public void close() throws IOException {
    }

}
