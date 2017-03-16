package org.endeavourhealth.hl7transform.transform.converters;

public class StringHelper {
    public static String formatName(String name) {
        if (name == null)
            return null;

        name = trimBetweenWords(name);

        String result = "";

        boolean previousWasLetter = false;

        for (int i = 0; i < name.length(); i++) {

            char character = name.charAt(i);

            if (previousWasLetter)
                result += Character.toString(character).toLowerCase();
            else
                result += Character.toString(character).toUpperCase();

            previousWasLetter = (Character.isLetter(character));
        }

        return result;
    }

    public static String trimBetweenWords(String str) {
        return str.replaceAll("\\s+"," ");
    }
}
