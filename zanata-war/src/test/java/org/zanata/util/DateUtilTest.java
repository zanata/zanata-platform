package org.zanata.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;

public class DateUtilTest {
//    @Test
//    public void testFormat() {
//        Date now = new Date();
//        System.out.println(DateUtil.formatShortDate(now));
//        System.out.println(DateUtil.formatTime(now));
//        System.out.println(DateUtil.getHowLongAgoDescription(new Date(0)));
//        System.out.println(DateUtil.getHowLongAgoDescription(Date.from(now.toInstant().minusMillis(10_000L))));
//        System.out.println(DateUtil.getHowLongAgoDescription(Date.from(now.toInstant().minusMillis(302_000L))));
//        System.out.println(DateUtil.getHowLongAgoDescription(Date.from(now.toInstant().minusMillis(3610_000L))));
//        System.out.println(DateUtil.getHowLongAgoDescription(Date.from(now.toInstant().minusMillis(72_600_000L))));
//        System.out.println(DateUtil.getHowLongAgoDescription(Date.from(now.toInstant().minusMillis(97_200_000L))));
//
//        System.out.println(DateUtil.getTimeRemainingDescription(10_000L));
//        System.out.println(DateUtil.getTimeRemainingDescription(302_000L));
//        System.out.println(DateUtil.getTimeRemainingDescription(3610_000L));
//        System.out.println(DateUtil.getTimeRemainingDescription(72_600_000L));
//        System.out.println(DateUtil.getTimeRemainingDescription(97_200_000L));
//
//        System.out.println(DateUtil.getUnitAndFigure(10_000L));
//        System.out.println(DateUtil.getUnitAndFigure(302_000L));
//        System.out.println(DateUtil.getUnitAndFigure(3610_000L));
//        System.out.println(DateUtil.getUnitAndFigure(72_600_000L));
//        System.out.println(DateUtil.getUnitAndFigure(97_200_000L));
//
//        System.out.println(DateUtil.getStartOfDay(now));
//        System.out.println(DateUtil.getEndOfTheDay(now));
//        System.out.println(DateUtil.getStartOfWeek(now));
//        System.out.println(DateUtil.getEndOfTheWeek(now));
//        System.out.println(DateUtil.getStartOfMonth(now));
//        System.out.println(DateUtil.getEndOfTheMonth(now));
//
//        System.out.println(DateUtil.getDate("31/12/1999 23:59", "dd/MM/yyyy HH:mm"));
//    }

    @Test
    public void testRanges() {
        Date now = new Date();

        // within range (true):
        Date soon = Date.from(now.toInstant().plusSeconds(60));
        Assert.assertTrue(DateUtil.isDatesInRange(now, soon, 1));
        Date tomorrow = Date.from(now.toInstant().plus(1, DAYS));
        Assert.assertTrue(DateUtil.isDatesInRange(now, tomorrow, 1));

        // outside range (false):
        Date tomorrowPlus = Date.from(now.toInstant().plus(1, DAYS).plusMillis(1));
        Assert.assertFalse(DateUtil.isDatesInRange(now, tomorrowPlus, 1));
        Date dayAfterTomorrow = Date.from(now.toInstant().plus(2, DAYS));
        Assert.assertFalse(DateUtil.isDatesInRange(now, dayAfterTomorrow, 1));
    }
}
