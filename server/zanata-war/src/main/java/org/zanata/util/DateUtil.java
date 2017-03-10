/**
 *
 */
package org.zanata.util;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DateUtil {

    private static final String DATE_TIME_SHORT_PATTERN = "dd/MM/yy HH:mm";
    private static final String TIME_SHORT_PATTERN = "hh:mm:ss";
    // Period Formatters are thread safe and immutable according to joda time
    // docs
    private static final PeriodFormatter TIME_REMAINING_FORMATTER =
            new PeriodFormatterBuilder().appendDays()
                    .appendSuffix(" day", " days").appendSeparator(", ")
                    .appendHours().appendSuffix(" hour", " hours")
                    .appendSeparator(", ").appendMinutes()
                    .appendSuffix(" min", " mins").toFormatter();

    /**
     * Format date to dd/MM/yy hh:mm a
     *
     * @param date
     * @return
     */
    public static String formatShortDate(Date date) {
        if (date != null) {
            DateTimeFormatter fmt =
                    DateTimeFormat.forPattern(DATE_TIME_SHORT_PATTERN);
            return fmt.print(new DateTime(date));
        }
        return null;
    }

    /**
     * Format date to hh:mm:ss
     *
     * @param date
     * @return
     */
    public static String formatTime(Date date) {
        if (date != null) {
            DateTimeFormatter fmt =
                    DateTimeFormat.forPattern(TIME_SHORT_PATTERN);
            return fmt.print(new DateTime(date));
        }
        return null;
    }

    /**
     * Return readable string of time different compare between 'then' and
     * current time e.g 10 minutes ago, 1 hour ago
     *
     * @param then
     * @return
     */
    public static String getHowLongAgoDescription(Date then) {
        Locale locale = Locale.getDefault();
        PrettyTime p = new PrettyTime(locale);
        return p.format(then);
    }

    public static String getTimeRemainingDescription(long durationInMillis) {
        Period period = new Period(durationInMillis);
        if (period.toStandardMinutes().getMinutes() <= 0) {
            return "less than a minute";
        } else {
            return TIME_REMAINING_FORMATTER.print(period.normalizedStandard());
        }
    }

    public static long getDurationInMillisecond(Date from, Date then) {
        return from.getTime() - then.getTime();
    }

    public static DateUnitAndFigure getUnitAndFigure(long durationInMillis) {
        Period period = new Period(durationInMillis);
        if (period.toStandardMinutes().getMinutes() <= 0) {
            return new DateUnitAndFigure("seconds",
                    period.toStandardSeconds().getSeconds());
        } else if (period.toStandardDays().getDays() <= 0) {
            return new DateUnitAndFigure("minutes",
                    period.toStandardMinutes().getMinutes());
        }
        return new DateUnitAndFigure("days", period.toStandardDays().getDays());
    }

    public static int compareDate(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return 0;
        }
        if (date1 == null || date2 == null) {
            return date1 == null ? -1 : 1;
        }
        return date1.compareTo(date2);
    }

    public static class DateUnitAndFigure {
        private String unit; // s(second) m(minute) or d(day)
        private int figure;

        public String getUnit() {
            return this.unit;
        }

        public int getFigure() {
            return this.figure;
        }

        @java.beans.ConstructorProperties({ "unit", "figure" })
        public DateUnitAndFigure(final String unit, final int figure) {
            this.unit = unit;
            this.figure = figure;
        }

        public DateUnitAndFigure() {
        }
    }

    /**
     * return start of the day date. e.g Tue Mar 25 12:31:00 EST 2014 returns
     * Tue Mar 25 00:00:00 EST 2014
     *
     * @param actionTime
     * @return
     */
    public static Date getStartOfDay(Date actionTime) {
        DateTime dateTime = new DateTime(actionTime);
        return dateTime.withTimeAtStartOfDay().toDate();
    }

    /**
     * return end of the day date. e.g Tue Mar 25 12:31:00 EST 2014 returns Tue
     * Mar 25 23:59:59 EST 2014
     *
     * @param actionTime
     * @return
     */
    public static Date getEndOfTheDay(Date actionTime) {
        DateTime endOfTheDay = new DateTime(actionTime).plusDays(1)
                .withTimeAtStartOfDay().minusMillis(1);
        return endOfTheDay.toDate();
    }

    /**
     * return first day of the week date. (Monday being the first day)
     *
     * @param actionTime
     * @return
     */
    public static Date getStartOfWeek(Date actionTime) {
        DateTime truncateMonth =
                new DateTime(actionTime).weekOfWeekyear().roundFloorCopy();
        return truncateMonth.toDate();
    }

    /**
     * return last day of the week date. (Sunday being the last day)
     *
     * @param actionTime
     * @return
     */
    public static Date getEndOfTheWeek(Date actionTime) {
        DateTime truncateMonth = new DateTime(actionTime).weekOfWeekyear()
                .roundCeilingCopy().minusMillis(1);
        return truncateMonth.toDate();
    }

    /**
     * return first day of the month date. e.g Tue Mar 25 12:31:00 EST 2014
     * returns Tue Mar 1 00:00:00 EST 2014
     *
     * @param actionTime
     * @return
     */
    public static Date getStartOfMonth(Date actionTime) {
        DateTime truncateMonth =
                new DateTime(actionTime).monthOfYear().roundFloorCopy();
        return truncateMonth.toDate();
    }

    /**
     * return last day of the month date. e.g Tue Mar 25 12:31:00 EST 2014
     * returns Tue Mar 31 23:59:59 EST 2014
     *
     * @param actionTime
     * @return
     */
    public static Date getEndOfTheMonth(Date actionTime) {
        DateTime truncateMonth = new DateTime(actionTime).monthOfYear()
                .roundCeilingCopy().minusMillis(1);
        return truncateMonth.toDate();
    }

    /**
     * Convert String to {@link java.util.Date} with given pattern
     *
     * @param date
     * @param pattern
     * @throws IllegalArgumentException
     */
    public static Date getDate(String date, String pattern)
            throws IllegalArgumentException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
        return formatter.parseDateTime(date).toDate();
    }

    /**
     * Check if date difference is within given days.
     *
     * @param from
     * @param to
     * @param days
     */
    public static boolean isDatesInRange(Date from, Date to, int days) {
        DateTime fromDate = new DateTime(from);
        DateTime toDate = new DateTime(to);
        Days d = Days.daysBetween(fromDate, toDate);
        return d.getDays() <= days;
    }
}
