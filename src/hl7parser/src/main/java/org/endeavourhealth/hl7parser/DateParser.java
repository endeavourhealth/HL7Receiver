package org.endeavourhealth.hl7parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class DateParser {
    // Format: YYYY[MM[DD[HHMM[SS[.S[S[S[S]]]]]]]][+/-ZZZZ]^<degree of precision>

    private static final int YYYY = 4;
    private static final int YYYYMM = 6;
    private static final int YYYYMMDD = 8;
    private static final int YYYYMMDDHHMM = 12;
    private static final int YYYYMMDDHHMMSS = 14;
    private static final int YYYYMMDDHHMMSS_T = 16;
    private static final int YYYYMMDDHHMMSS_TT = 17;
    private static final int YYYYMMDDHHMMSS_TTT = 18;
    private static final int YYYYMMDDHHMMSS_TTTT = 19;


    public static LocalDateTime parse(String dateTime) throws ParseException {
        Validate.notNull(dateTime);

        dateTime = StringUtils.deleteWhitespace(dateTime);

        dateTime = removeSecondComponent(dateTime);

        if (dateTime == "")
            return null;


        if (!isValidTs(dateTime))
            throw new ParseException("Invalid date/time");

        String timeZone = getTimeZone(dateTime);
        dateTime = removeTimeZone(dateTime);

        if (dateTime.length() < 8)
            dateTime = handleShortDates(dateTime);

        String pattern = getPattern(dateTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        if (dateTime.length() == 8)
            return LocalDate.parse(dateTime, formatter).atStartOfDay();

        return LocalDateTime.parse(dateTime, formatter);
    }

    private static String handleShortDates(String dateTime) {
        if (dateTime.length() == 4)
            return dateTime + "0101";

        if (dateTime.length() == 6)
            return dateTime + "01";

        return dateTime;
    }

    private static String getTimeZonePattern(String timeZone) {
        if (timeZone.length() > 0)
            return "Z";

        return "";
    }

    public static String getPattern(String dateTime) throws ParseException {
        switch (dateTime.length()) {
            case YYYY: return "yyyy";
            case YYYYMM: return "yyyy-MM";
            case YYYYMMDD: return "yyyyMMdd";
            case YYYYMMDDHHMM: return "yyyyMMddHHmm";
            case YYYYMMDDHHMMSS: return "yyyyMMddHHmmss";
            case YYYYMMDDHHMMSS_T: return "yyyyMMddHHmmss.S";
            case YYYYMMDDHHMMSS_TT: return "yyyyMMddHHmmss.SS";
            case YYYYMMDDHHMMSS_TTT: return "yyyyMMddHHmmss.SSS";
            case YYYYMMDDHHMMSS_TTTT: return "yyyyMMddHHmmss.SSSS";
            default: throw new ParseException("Could not parse date time");
        }
    }

    private static String removeTimeZone(String dateTime) {
        if (StringUtils.contains(dateTime, "+"))
            return Helpers.split(dateTime, "+").get(0);
        else if (StringUtils.contains(dateTime, "-"))
            return Helpers.split(dateTime, "-").get(0);

        return dateTime;
    }

    private static String getTimeZone(String dateTime) {
        if (StringUtils.contains(dateTime, "+"))
            return Helpers.split(dateTime, "+").get(1);
        else if (StringUtils.contains(dateTime, "-"))
            return Helpers.split(dateTime, "-").get(1);

        return "";
    }

    private static String removeSecondComponent(String dateTime) throws ParseException {
        List<String> pieces = Helpers.split(dateTime, "^");

        if (pieces.size() > 2)
            throw new ParseException("Unrecognised date/time");

        return pieces.get(0).trim();
    }

    public static boolean isValidTs(String dateTime) {
        if (dateTime == null)
            return false;

        String regex = "([12]\\d{3}" + "((0[1-9]|1[0-2])"
                + "((0[1-9]|[12]\\d|3[01])" + "(([01]\\d|2[0-3])" + "([0-5]\\d"
                + "([0-5]\\d" + "(\\.\\d\\d?\\d?\\d?)?)?)?)?)?)?"
                + "((\\+|\\-)([01]\\d|2[0-3])[0-5]\\d)?)?";
        return dateTime.matches(regex);
    }
}