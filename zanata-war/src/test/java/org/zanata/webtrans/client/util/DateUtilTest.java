package org.zanata.webtrans.client.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

@Test(groups = { "unit-tests" })
public class DateUtilTest {

    private Date aDate;

    @BeforeMethod
    public void before() {
        aDate = new GregorianCalendar(2013, 11, 25, 1, 2, 3).getTime();
    }

    @Test
    public void testFormatShortDate() throws Exception {
        String s = DateUtil.formatShortDate(aDate);
        assertEquals(s, "25/12/13 01:02");
    }

    @Test
    public void testFormatLongDateTime() throws Exception {
        String s = DateUtil.formatLongDateTime(aDate);
        assertEquals(s, "25/12/13 01:02:03");
    }
}
