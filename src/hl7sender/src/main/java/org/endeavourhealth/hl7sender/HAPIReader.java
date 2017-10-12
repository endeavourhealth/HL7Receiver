package org.endeavourhealth.hl7sender;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class HAPIReader extends AbstractMessageReader {

    private Hl7InputStreamMessageIterator hapiIter;

    public void prepare() throws FileNotFoundException {
        FileReader reader = new FileReader(getInputMessages());
        hapiIter = new Hl7InputStreamMessageIterator(reader);
    }

    public boolean hasNext() {
        return hapiIter.hasNext();
    }

    @Override
    public Message next() {
        return hapiIter.next();
    }

    public void postSend() {

    }

    @Override
    public void close() throws IOException {
        hapiIter = null;
    }

}
