package org.zanata.webtrans.client.util;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TextFormatUtil {

    public static String formatPercentage(double percentage) {
        return String.valueOf(Math.floor(percentage));
    }

    public static String formatHours(double hours) {
        return String.valueOf(Math.ceil(hours * 100.0) / 100);
    }
}
