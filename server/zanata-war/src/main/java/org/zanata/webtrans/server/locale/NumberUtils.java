package org.zanata.webtrans.server.locale;

public class NumberUtils {
    private static final String[] WORDS = { "none", "one", "two", "three",
            "four", "five", " six", " seven", "eight", " nine", "ten", "many" };

    public static String getWords(int i) {
        String result = null;
        if (i >= 0 && i <= 10) {
            result = WORDS[i];
        } else if (i > 10) {
            result = WORDS[11];
        }

        return result;
    }
}
