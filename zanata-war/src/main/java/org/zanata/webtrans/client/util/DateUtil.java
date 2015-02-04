/**
 *
 */
package org.zanata.webtrans.client.util;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Date;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@NotThreadSafe
public class DateUtil {
    private static final DefaultDateTimeFormatInfo info =
            new DefaultDateTimeFormatInfo();
    private final static String DATE_TIME_SHORT_PATTERN = "dd/MM/yy HH:mm";
    private static DateTimeFormat dtfShort = new DateTimeFormat(DATE_TIME_SHORT_PATTERN, info) {};
    private final static String DATE_TIME_LONG_PATTERN = "dd/MM/yy HH:mm:ss";
    private static DateTimeFormat dtfLong = new DateTimeFormat(DATE_TIME_LONG_PATTERN, info) {};

    /**
     * Format date to dd/MM/yy hh:mm a
     *
     * @param date
     * @return
     */
    public static String formatShortDate(Date date) {
        if (date != null) {
            return dtfShort.format(date);
        }
        return null;
    }

    /**
     * Format date to dd/MM/yy hh:mm:ss
     *
     * @param date
     * @return
     */
    public static String formatLongDateTime(Date date) {
        if (date != null) {
            return dtfLong.format(date);
        }
        return null;
    }
}
