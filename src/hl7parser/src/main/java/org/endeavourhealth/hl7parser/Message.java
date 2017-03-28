package org.endeavourhealth.hl7parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7parser.segments.MshSegment;
import org.endeavourhealth.hl7parser.segments.SegmentName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Message {
    private static final String CR = "\r";
    private static final String LF = "\n";
    private static final int FIRST = 0;
    private static final String MSH_SEGMENT_NAME = "MSH";

    private final String originalMessageText;    // originalMessageText may not reflect the current state of the message
    private HashMap<String, Class<? extends Segment>> zSegmentDefinitions;
    private Seperators seperators;
    private List<Segment> segments;

    //////////////////  Constructors  //////////////////

    public Message(final String messageText) throws ParseException {
        this(messageText, null);
    }

    public Message(final String messageText, HashMap<String, Class<? extends Segment>> zSegmentDefinitions) throws ParseException {
        Validate.notBlank(messageText);

        this.originalMessageText = messageText;
        this.zSegmentDefinitions = zSegmentDefinitions;
        parse(messageText);
    }

    //////////////////  Accessors  //////////////////

    public boolean hasSegment(String segmentName) {
        return (getSegments(segmentName).size() > 0);
    }

    public <T extends Segment> T getSegment(String segmentName, Class<T> segmentClass) {
        Segment segment = getSegment(segmentName);

        if ((segment == null) || (segmentClass.isAssignableFrom(segment.getClass())))
            return segmentClass.cast(segment);

        throw new ClassCastException("Failed cast from " + segment.getClass().toString() + " to " + segmentClass.getClass().toString());
    }

    public List<Segment> getSegments() {
        return this.segments;
    }

    public Segment getSegment(String segmentName) {
        List<? extends Segment> segments = getSegments(segmentName);
        return Helpers.getSafely(segments, FIRST);
    }

    public <T extends Segment> List<T> getSegments(String segmentName, Class<T> segmentClass) {
        List<? extends Segment> segments = getSegments(segmentName);
        return (List<T>)segments;
    }

    public List<? extends Segment> getSegments(String segmentName) {
        Validate.notBlank(segmentName);

        return this.segments
                .stream()
                .filter(t -> t.getSegmentName().equals(segmentName))
                .collect(Collectors.toList());
    }

    public long getSegmentCount(String segmentName) {
        Validate.notBlank(segmentName);

        return this.segments
                .stream()
                .filter(t -> t.getSegmentName().equals(segmentName))
                .count();
    }

    public List<Component> getAllComponents() {
        return getSegments()
                .stream()
                .flatMap(t -> t.getAllComponents().stream())
                .collect(Collectors.toList());
    }

    //////////////////  Parsers  //////////////////

    private void parse(String messageText) throws ParseException {
        String cleanedMessageText = normaliseLineEndings(messageText);

        this.seperators = detectSeperators(cleanedMessageText);
        this.segments = parseSegments(cleanedMessageText, seperators, this.zSegmentDefinitions);
    }

    private static String normaliseLineEndings(String message) {
        message = message.trim();
        message = message.replace(LF, CR);

        while (true) {
            int messageLength = message.length();

            message = message.replace(CR + CR, CR);

            if (message.length() == messageLength)
                break;
        }

        return message;
    }

    private static Seperators detectSeperators(String messageText) throws ParseException {
        Seperators seperators = new Seperators();

        String firstLine = StringUtils.split(messageText, seperators.getLineSeperator())[FIRST];

        if (!firstLine.startsWith(MSH_SEGMENT_NAME))
            throw new ParseException("message does not start with " + MSH_SEGMENT_NAME + " segment");

        String firstLineWithoutSegmentName = StringUtils.removeStart(firstLine, MSH_SEGMENT_NAME);

        if (firstLineWithoutSegmentName.length() < 5)
            throw new ParseException(MSH_SEGMENT_NAME + " does not encoding characters");

        seperators
                .setFieldSeperator(firstLineWithoutSegmentName.substring(0, 1))
                .setComponentSeperator(firstLineWithoutSegmentName.substring(1, 2))
                .setRepetitionSeperator(firstLineWithoutSegmentName.substring(2, 3))
                .setEscapeCharacter(firstLineWithoutSegmentName.substring(3, 4))
                .setSubcomponentSeperator(firstLineWithoutSegmentName.substring(4, 5));

        if (!seperators.areSeperatorsUnique())
            throw new ParseException("Seperators are not unique");

        if (!messageText.contains(seperators.getFieldSeperator()))
            throw new ParseException("Field seperator does not appear to be correct");

        return seperators;
    }

    private static List<Segment> parseSegments(String messageText, Seperators seperators, HashMap<String, Class<? extends Segment>> zSegmentDefinitions) throws ParseException {
        List<Segment> segments = new ArrayList<>();

        List<String> lines = Helpers.split(messageText, seperators.getLineSeperator());

        for (String line : lines)
            segments.add(Segment.parseAndinstantiate(line, seperators, zSegmentDefinitions));

        return segments;
    }

    //////////////////  Composers  //////////////////

    public String compose() {
        return String.join(this.seperators.getLineSeperator(),
                getSegments()
                        .stream()
                        .map(t -> t.compose())
                        .collect(Collectors.toList()));
    }
}
