package org.zanata.rest.service;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.zanata.exception.InvalidDateParamException;
import org.zanata.util.DateUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
* @author Patrick Huang
*         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class DateRange {
    private static final int MAX_STATS_DAYS = 365;
    @Getter
    private final DateTime fromDate;
    @Getter
    private final DateTime toDate;
    @Getter
    private final DateTimeZone timeZone;

    static DateRange from(String dateRangeParam) {
        return from(dateRangeParam, null);
    }

    static DateRange from(String dateRangeParam, String fromTimezoneId) {
        String[] dateRange = dateRangeParam.split("\\.\\.");
        if (dateRange.length != 2) {
            throw new InvalidDateParamException(dateRangeParam);
        }
        // here we use java util timezone first because it supports more short IDs
        DateTimeZone zone = (fromTimezoneId == null ? DateTimeZone.getDefault() :
                DateTimeZone.forTimeZone(TimeZone.getTimeZone(fromTimezoneId)));

        DateTime fromDate;
        DateTime toDate;

        try {
            DateTimeFormatter formatter =
                    DateTimeFormat.forPattern(StatisticsResource.DATE_FORMAT)
                            .withZone(zone);
            fromDate = formatter.parseDateTime(dateRange[0]);
            toDate = formatter.parseDateTime(dateRange[1]);

            fromDate = fromDate.withTimeAtStartOfDay(); // start of day
            toDate = toDate.plusDays(1).minusMillis(1); // end of day

            if (fromDate.isAfter(toDate) || Days.daysBetween(fromDate,
                    toDate).getDays() > MAX_STATS_DAYS) {
                throw new InvalidDateParamException(dateRangeParam);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidDateParamException(dateRangeParam);
        }
        return new DateRange(fromDate, toDate, zone);
    }
}
