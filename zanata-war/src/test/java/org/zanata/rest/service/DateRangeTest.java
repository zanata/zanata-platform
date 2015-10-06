package org.zanata.rest.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateRangeTest {

    @Test
    public void testConcept() {
        ZoneId bneZone = ZoneId.of("Australia/Brisbane");
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai"); // 2 hours late

        ZonedDateTime bneTime = ZonedDateTime.of(2015, 2, 1, 0, 0, 0, 0, bneZone);
        ZonedDateTime shanghaiTime = ZonedDateTime.of(2015, 2, 1, 0, 0, 0, 0, shanghaiZone);
        Assertions.assertThat(Duration.between(bneTime, shanghaiTime)).isEqualTo(Duration.of(2, ChronoUnit.HOURS));

        ZonedDateTime bneToShanghai = bneTime.withZoneSameInstant(shanghaiZone);
        Assertions.assertThat(bneToShanghai.getMonthValue()).isEqualTo(1);
        Assertions.assertThat(bneToShanghai.getDayOfMonth()).isEqualTo(31);
        Assertions.assertThat(bneToShanghai.getHour()).isEqualTo(22);

        // now test our parser
        DateTimeFormatter bneFormat =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(bneZone);

        DateTimeFormatter shanghaiFormat = bneFormat.withZone(shanghaiZone);
        ZonedDateTime timeAsShanghai = ZonedDateTime.parse("2015-02-01 00:00:00", shanghaiFormat);
        ZonedDateTime timeInBne = timeAsShanghai.withZoneSameInstant(bneZone);
        Assertions.assertThat(timeInBne.getMonthValue()).isEqualTo(2);
        Assertions.assertThat(timeInBne.getHour()).isEqualTo(2);

        timeAsShanghai = ZonedDateTime.parse("2015-02-01 00:00:00", shanghaiFormat);
        ZonedDateTime endOfDayShanghai = timeAsShanghai.plusDays(1).minus(1, ChronoUnit.MILLIS);

        timeInBne = endOfDayShanghai.withZoneSameInstant(bneZone);
        Assertions.assertThat(timeInBne.getMonthValue()).isEqualTo(2);
        Assertions.assertThat(timeInBne.getDayOfMonth()).isEqualTo(2);

    }

}
