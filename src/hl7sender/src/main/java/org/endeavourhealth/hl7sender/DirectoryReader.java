package org.endeavourhealth.hl7sender;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class DirectoryReader extends AbstractMessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryReader.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private Path currentFile = null;
    List<Path> fileList = null;
    private int fileListPos = 0;

    /*
     *
     */
    public void prepare() throws IOException {
        Path dir = Paths.get(getInputMessages().getAbsolutePath());
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        fileList = new ArrayList<>();
        //stream.forEach(fileList::add);
        stream.forEach((p) -> {
            if (Files.isDirectory(p) == false) {
                fileList.add(p);
            }
        });
        if (fileList != null) {
            fileList.sort(Comparator.comparing(Path::toString));
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
        archiveFileList = null;
    }

    public boolean hasNext() {
        if ((fileList != null) && (fileList.size() > 0) && (fileListPos < fileList.size())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Message next() throws IOException {
        Message ret = null;

        while (hasNext() && ret == null) {
            currentFile = fileList.get(fileListPos);
            fileListPos++;
            LOG.info("Next file in list(" + currentFile.getFileName().toString() + "):" + currentFile.toString());

            if (skipMessages) {
                if (lastSuccessSendMsgId.compareTo(currentFile.getFileName().toString()) == 0) {
                    LOG.info("Skip message (for the last time):" + currentFile.getFileName().toString());
                    skipMessages = false;
                } else {
                    LOG.info("Skip message:" + currentFile.getFileName().toString());
                }
            } else {
                FileReader reader = new FileReader(currentFile.toFile());
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
        LOG.info("CurrFile:" + currentFile.getFileName().toString());
        File newFile = new File(getArchiveDir().getAbsolutePath() + File.separator + currentFile.getFileName().toString());
        if (newFile.exists()) {
            LOG.info("File " + newFile.getAbsolutePath() + " already in archive");
        } else {
            Path n = Paths.get(newFile.getAbsolutePath());
            LOG.info("Copying file:" + currentFile.toString() + " To:" + n.toString());
            Files.copy(currentFile, n, REPLACE_EXISTING);
        }
        LOG.info("File archived successfully");
    }

    @Override
    public void close() throws IOException {
    }

}
