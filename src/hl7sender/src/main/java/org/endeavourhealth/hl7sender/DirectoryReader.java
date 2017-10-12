package org.endeavourhealth.hl7sender;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator;
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
import java.util.Date;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class DirectoryReader extends AbstractMessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryReader.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private File currentFile = null;

    public void prepare() throws FileNotFoundException {
    }

    public boolean hasNext() {
        return (getInputMessages().listFiles().length > 0);
    }

    @Override
    public Message next() throws IOException {
        currentFile = getInputMessages().listFiles()[0];
        FileReader reader = new FileReader(currentFile);
        Hl7InputStreamMessageIterator hapiIter = new Hl7InputStreamMessageIterator(reader);
        Message ret = hapiIter.next();
        reader.close();
        return ret;
    }

    public void postSend() throws IOException {
        //String fileAddOn = UUID.randomUUID().toString();
        String fileAddOn = dateFormat.format(new Date()).replaceAll(":", "-");
        File newFile = new File(getArchiveDir().getAbsolutePath() + File.separator + currentFile.getName() + "." + fileAddOn);
        LOG.info("Moving file:" + currentFile.getAbsolutePath() + " To:" + newFile.getAbsolutePath());
        Path c = Paths.get(currentFile.getAbsolutePath());
        Path n = Paths.get(newFile.getAbsolutePath());
        Files.move(c, n, REPLACE_EXISTING);
    }

    @Override
    public void close() throws IOException {
    }

}
