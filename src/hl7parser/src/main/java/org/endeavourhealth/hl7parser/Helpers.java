package org.endeavourhealth.hl7parser;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Helpers {
    public static Integer parseInteger(String value) throws ParseException {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new ParseException("Error parsing integer", e);
        }
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static <T extends Object> T getSafely(List<T> list, int index) {
        if (list == null)
            return null;

        if ((index >= 0) && (index <= (list.size() - 1)))
            return list.get(index);

        return null;
    }

    public static String formatString(String pattern, Object ... arguments) {
        return MessageFormat.format(pattern, arguments);
    }

    public static List<String> split(String str, String seperator) {
        if (str == null)
            return null;

        // fix anomoly with StringUtils.splitByWholeSeparatorPreserveAllTokens
        //
        // if split("|", "|") == { "", "" }
        // then split("", "|") should == { "" }
        //
        // (as per c#)
        //
        if (str.equals(""))
            return Arrays.asList(new String[] { "" });

        return Arrays.asList(StringUtils.splitByWholeSeparatorPreserveAllTokens(str, seperator));
    }
}
