package org.zanata.rest.service;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class DateRangeTest {

    @Test
    public void testConcept() {

        DateTimeZone bneZone = DateTimeZone.forID("Australia/Brisbane");
        DateTimeZone shanghaiZone = DateTimeZone.forID("Asia/Shanghai"); // 2 hours late

        DateTime bneTime = new DateTime(2015, 2, 1, 0, 0, bneZone);
        DateTime shanghaiTime = new DateTime(2015, 2, 1, 0, 0, shanghaiZone);
        int hours = Hours.hoursBetween(bneTime, shanghaiTime).getHours();
        Assertions.assertThat(hours).isEqualTo(2);

        DateTime bneToShanghai = bneTime.toDateTime(shanghaiZone);
        Assertions.assertThat(bneToShanghai.getMonthOfYear()).isEqualTo(1);
        Assertions.assertThat(bneToShanghai.getDayOfMonth()).isEqualTo(31);
        Assertions.assertThat(bneToShanghai.getHourOfDay()).isEqualTo(22);

        // now test our parser
        DateTimeFormatter bneFormat =
                DateTimeFormat.forPattern("yyyy-MM-dd").withZone(bneZone);

        DateTimeFormatter shanghaiFormat = bneFormat.withZone(shanghaiZone);
        DateTime timeAsShanghai = shanghaiFormat.parseDateTime("2015-02-01");
        DateTime timeInBne = timeAsShanghai.toDateTime(bneZone);
        Assertions.assertThat(timeInBne.getMonthOfYear()).isEqualTo(2);
        Assertions.assertThat(timeInBne.getHourOfDay()).isEqualTo(2);

        timeAsShanghai = shanghaiFormat.parseDateTime("2015-02-01");
        DateTime endOfDayShanghai = timeAsShanghai.plusDays(1).minusMillis(1);

        timeInBne = endOfDayShanghai.toDateTime(bneZone);
        Assertions.assertThat(timeInBne.getMonthOfYear()).isEqualTo(2);
        Assertions.assertThat(timeInBne.getDayOfMonth()).isEqualTo(2);

    }

}
