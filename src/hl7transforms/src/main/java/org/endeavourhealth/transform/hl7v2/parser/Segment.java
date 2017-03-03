package org.endeavourhealth.transform.hl7v2.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.parser.segments.SegmentName;
import org.endeavourhealth.transform.hl7v2.transform.Hl7DateTime;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Segment {
    private static int FIRST = 0;
    private static int SEGMENT_NAME_LENGTH = 3;

    private String originalSegmentText;    // originalSegmentText may not reflect the current state of the segment
    private Seperators seperators;
    private String segmentName;
    protected List<Field> fields = new ArrayList<>();

    //////////////////  Constructors  //////////////////

    public static Segment parseAndinstantiate(String segmentText, Seperators seperators, HashMap<String, Class<? extends Segment>> zSegmentDefinitions) throws ParseException {
        Validate.notBlank(segmentText);
        Validate.notNull(seperators);

        String segmentName = getSegmentName(segmentText, seperators);
        Class<? extends Segment> segmentClass = getSegmentClass(segmentName, zSegmentDefinitions);

        try {
            Constructor<? extends Segment> constructor = segmentClass.getConstructor(String.class, Seperators.class);

            if (constructor == null)
                throw new ParseException("Could not find constructor for segment " + segmentClass);

            return constructor.newInstance(segmentText, seperators);
        } catch (Exception e) {
            throw new ParseException("Could not instantiate segment " + segmentClass);
        }
    }

    private static Class<? extends Segment> getSegmentClass(String segmentName, HashMap<String, Class<? extends Segment>> zSegmentDefinitions) {
        Class<? extends Segment> segmentClass = SegmentName.getSegmentClass(segmentName);

        if (segmentClass == null)
            if (zSegmentDefinitions != null)
                segmentClass = zSegmentDefinitions.getOrDefault(segmentName, null);

        if (segmentClass == null)
            segmentClass = Segment.class;

        return segmentClass;
    }

    private Segment() {
    }

    public Segment(String segmentText, Seperators seperators) throws ParseException {
        Validate.notBlank(segmentText);
        Validate.notNull(seperators);

        this.originalSegmentText = segmentText;
        this.seperators = seperators;

        this.parse();
    }

    //////////////////  Accessors  //////////////////

    public String getSegmentName() {
        return this.segmentName;
    }

    public Field getField(int fieldNumber) {
        int fieldIndex = fieldNumber - 1;

        return Helpers.getSafely(this.fields, fieldIndex);
    }

    public List<Field> getFields() {
        return this.fields;
    }

    public <T extends Datatype> T getFieldAsDatatype(int fieldNumber, Class<T> datatype) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getDatatype(datatype);
    }

    public <T extends Datatype> List<T> getFieldAsDatatypes(int fieldNumber, Class<T> datatype) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getDatatypes(datatype);
    }

    public String getComponentAsString(int fieldNumber, int componentNumber) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getComponentAsString(componentNumber);
    }

    public Hl7DateTime getFieldAsHl7Date(int fieldNumber) throws ParseException {
        String field = getFieldAsString(fieldNumber);

        if (StringUtils.isBlank(field))
            return null;

        return new Hl7DateTime(field);
    }

    public Integer getFieldAsInteger(int fieldNumber) throws ParseException {
        String field = getFieldAsString(fieldNumber);

        if (StringUtils.isBlank(field))
            return null;

        return Integer.parseInt(field);
    }

    public String getFieldAsString(int fieldNumber) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getAsString();
    }

    public List<String> getFieldAsStringList(int fieldNumber) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getDatatypesAsString();
    }

    public List<Component> getAllComponents() {
        return getFields()
                    .stream()
                    .flatMap(s -> s.getGenericDatatypes()
                                    .stream()
                                    .flatMap(r -> r.getComponents().stream()))
                .collect(Collectors.toList());
    }

    //////////////////  Parsers  //////////////////

    private static String getSegmentName(String segment, Seperators seperators) throws ParseException {
        List<String> tokens = Arrays.asList(StringUtils.split(segment, seperators.getFieldSeperator()));

        if (tokens.get(FIRST).length() != SEGMENT_NAME_LENGTH)
            throw new ParseException("Segment name is not three characters");

        return tokens.get(FIRST);
    }

    private void parse() throws ParseException {
        this.segmentName = getSegmentName(originalSegmentText, seperators);

        List<String> tokens =
                Helpers.split(originalSegmentText, seperators.getFieldSeperator())
                .stream()
                .skip(1)
                .collect(Collectors.toList());

        if (SegmentName.MSH.equals(this.segmentName))
            tokens.add(FIRST, seperators.getFieldSeperator());

        for (String token : tokens)
            this.fields.add(new Field(token, this.seperators));
    }

    //////////////////  Composers  //////////////////

    public String compose() {
        List<Field> fields = this.getFields();

        if (SegmentName.MSH.equals(this.segmentName)) {
            fields = fields
                    .stream()
                    .skip(1)
                    .collect(Collectors.toList());
        }

        return this.getSegmentName()
                + this.seperators.getFieldSeperator()
                + String.join(this.seperators.getFieldSeperator(),
                fields
                        .stream()
                        .map(t -> t.compose())
                        .collect(Collectors.toList()));
    }
}
