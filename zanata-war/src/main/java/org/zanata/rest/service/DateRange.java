package org.zanata.rest.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.jboss.resteasy.spi.BadRequestException;
import org.zanata.exception.InvalidDateParamException;
import org.zanata.util.DateUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.time.temporal.ChronoUnit.DAYS;

/**
* @author Patrick Huang
*         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DateRange {
    private static final int MAX_STATS_DAYS = 365;
    private static final Duration MAX_STATS_DURATION = Duration.ofDays(MAX_STATS_DAYS);

    @Getter
    private final ZonedDateTime fromDate;
    @Getter
    private final ZonedDateTime toDate;
    @Getter
    private final ZoneId timeZone;

    public static DateRange from(String dateRangeParam) {
        return from(dateRangeParam, null);
    }

    public static DateRange from(String dateRangeParam, String fromTimezoneId) {
        String[] dateRange = dateRangeParam.split("\\.\\.");
        if (dateRange.length != 2) {
            throw new InvalidDateParamException("Invalid data range: " + dateRangeParam);
        }
        ZoneId zone;
        if (fromTimezoneId == null) {
            zone = ZoneId.systemDefault();
        } else {
            try {
                zone = ZoneId.of(fromTimezoneId);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid timezone ID:" + fromTimezoneId);
            }
        }

        ZonedDateTime fromDate;
        ZonedDateTime toDate;

        try {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(StatisticsResource.DATE_FORMAT)
                            .withZone(zone);
            fromDate = ZonedDateTime.parse(dateRange[0], formatter);
            toDate = ZonedDateTime.parse(dateRange[1], formatter);

            fromDate = fromDate.truncatedTo(DAYS); // start of day
            toDate = toDate.truncatedTo(DAYS).plusDays(1).minusNanos(1000_000L); // end of day

            Duration duration = Duration.between(fromDate, toDate);
            if (duration.isNegative() || duration.compareTo(MAX_STATS_DURATION) > 0) {
                throw new InvalidDateParamException("Invalid data range: " + dateRangeParam);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidDateParamException("Invalid data range: " + dateRangeParam);
        }
        return new DateRange(fromDate, toDate, zone);
    }
}
