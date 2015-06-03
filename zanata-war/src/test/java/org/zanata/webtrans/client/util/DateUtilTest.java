package org.zanata.webtrans.client.util;


import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilTest {

    private Date aDate;

    @Before
    public void before() {
        aDate = new GregorianCalendar(2013, 11, 25, 1, 2, 3).getTime();
    }

    @Test
    public void testFormatShortDate() throws Exception {
        String s = DateUtil.formatShortDate(aDate);
        assertThat(s).isEqualTo("25/12/13 01:02");
    }

    @Test
    public void testFormatLongDateTime() throws Exception {
        String s = DateUtil.formatLongDateTime(aDate);
        assertThat(s).isEqualTo("25/12/13 01:02:03");
    }
}
