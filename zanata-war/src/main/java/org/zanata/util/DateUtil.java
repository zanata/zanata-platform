/**
 *
 */
package org.zanata.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.LongFunction;

import com.ibm.icu.impl.duration.BasicPeriodFormatterService;
import com.ibm.icu.impl.duration.DurationFormatter;
import lombok.ToString;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class DateUtil {
    private final static String DATE_TIME_SHORT_PATTERN = "dd/MM/yy HH:mm";
    private final static String TIME_SHORT_PATTERN = "hh:mm:ss";
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DurationFormatter TIME_REMAINING_FORMATTER =
            BasicPeriodFormatterService.getInstance().newDurationFormatterFactory().getFormatter();
    // one millisecond in nanos
    private static final long ONE_MILLI = 1000_000L;

    /**
     * Format date to dd/MM/yy hh:mm a
     *
     * @param date
     * @return
     */
    public static String formatShortDate(Date date) {
        if (date != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                    DATE_TIME_SHORT_PATTERN).withZone(ZoneId.systemDefault());
            return fmt.format(date.toInstant());
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
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                    TIME_SHORT_PATTERN).withZone(ZoneId.systemDefault());
            return fmt.format(date.toInstant());
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
        long now = new Date().getTime();
        long durationInMillis = now - then.getTime();
        return getDurationDescription(durationInMillis, d ->
                TIME_REMAINING_FORMATTER.formatDurationFrom(-durationInMillis, now));
    }

    public static String getTimeRemainingDescription(long durationInMillis) {
        return getDurationDescription(durationInMillis, d ->
                TIME_REMAINING_FORMATTER.formatDurationFromNow(
                        durationInMillis)
        );
    }

    private static String getDurationDescription(long durationInMillis,
                                                LongFunction<String> format) {
        if (durationInMillis < 60_000) {
            return "less than a minute";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(format.apply(durationInMillis));
            Duration d = Duration.ofMillis(durationInMillis);
            sb.append(" (");
            if (d.toDays() != 0) {
                sb.append(d.toDays()).append("d ");
            }
            sb.append(LocalTime.MIDNIGHT.plus(d).format(
                    DateTimeFormatter.ofPattern("H'h':mm'm'")));
            sb.append(")");
            return sb.toString();
        }
    }

    public static long getDurationInMillisecond(Date from, Date then) {
        return from.getTime() - then.getTime();
    }

    public static DateUnitAndFigure getUnitAndFigure(long durationInMillis) {
        Duration period = Duration.ofMillis(durationInMillis);
        if (period.toMinutes() <= 0) {
            return new DateUnitAndFigure("seconds", period.getSeconds());
        } else if (period.toDays() <= 0) {
            return new DateUnitAndFigure("minutes", period.toMinutes());
        }
        return new DateUnitAndFigure("days", period.toDays());
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

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DateUnitAndFigure {
        private String unit; // s(second) m(minute) or d(day)
        private long figure;
    }

    /**
     * return start of the day date. e.g Tue Mar 25 12:31:00 EST 2014 returns
     * Tue Mar 25 00:00:00 EST 2014
     *
     * @param actionTime
     * @return
     */
    public static Date getStartOfDay(Date actionTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(actionTime.toInstant(), ZONE);
        LocalDateTime start = ldt.toLocalDate().atStartOfDay();
        return Date.from(start.atZone(ZONE).toInstant());
    }

    /**
     * return end of the day date. e.g Tue Mar 25 12:31:00 EST 2014 returns Tue
     * Mar 25 23:59:59 EST 2014
     *
     * @param actionTime
     * @return
     */
    public static Date getEndOfTheDay(Date actionTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(actionTime.toInstant(), ZONE);
        LocalDateTime end = ldt.toLocalDate().atStartOfDay().plusDays(1).minusNanos(ONE_MILLI);
        return Date.from(end.atZone(ZONE).toInstant());
    }

    /**
     * return first day of the week date. (Monday being the first day)
     *
     * @param actionTime
     * @return
     */
    public static Date getStartOfWeek(Date actionTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(actionTime.toInstant(), ZONE);
        LocalDateTime start = ldt.truncatedTo(DAYS).with(previousOrSame(MONDAY));
        return Date.from(start.atZone(ZONE).toInstant());
    }

    /**
     * return last day of the week date. (Sunday being the last day)
     *
     * @param actionTime
     * @return
     */
    public static Date getEndOfTheWeek(Date actionTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(actionTime.toInstant(), ZONE);
        LocalDateTime end = ldt.truncatedTo(DAYS).with(previousOrSame(MONDAY)).plusWeeks(1).minusNanos(ONE_MILLI);
        return Date.from(end.atZone(ZONE).toInstant());
    }

    /**
     * return first day of the month date. e.g Tue Mar 25 12:31:00 EST 2014
     * returns Tue Mar 1 00:00:00 EST 2014
     *
     * @param actionTime
     * @return
     */
    public static Date getStartOfMonth(Date actionTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(actionTime.toInstant(), ZONE);
        LocalDateTime start = ldt.truncatedTo(DAYS).withDayOfMonth(1);
        return Date.from(start.atZone(ZONE).toInstant());
    }

    /**
     * return last day of the month date. e.g Tue Mar 25 12:31:00 EST 2014
     * returns Tue Mar 31 23:59:59 EST 2014
     *
     * @param actionTime
     * @return
     */
    public static Date getEndOfTheMonth(Date actionTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(actionTime.toInstant(), ZONE);
        LocalDateTime start = ldt.truncatedTo(DAYS).withDayOfMonth(1).plusMonths(1).minusNanos(ONE_MILLI);
        return Date.from(start.atZone(ZONE).toInstant());
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
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(pattern);
        LocalDateTime ldt = LocalDateTime.parse(date, formatter);
        return Date.from(ldt.atZone(ZONE).toInstant());
    }

    /**
     * Check that date difference is no more than 'days' days.
     *
     * @param from
     * @param to
     * @param days
     */
    public static boolean isDatesInRange(Date from, Date to, int days) {
        long durationMillis = to.getTime() - from.getTime();
        Duration actual = Duration.ofMillis(durationMillis);
        Duration limit = Duration.ofDays(days);
        return actual.compareTo(limit) <= 0;
    }
}
