package org.zanata.webtrans.server.locale;

public class NumberUtils {
    public static final String[] Words = { "none", "one", "two", "three",
            "four", "five", " six", " seven", "eight", " nine", "ten", "many" };

    public static String getWords(int i) {
        String result = null;
        if (i >= 0 && i <= 10) {
            result = Words[i];
        } else if (i > 10) {
            result = Words[11];
        }

        return result;
    }
}
