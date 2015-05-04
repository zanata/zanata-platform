/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.feature.testharness;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.zanata.page.WebDriverFactory;

/**
 * Global application of rules to Zanata functional tests
 *
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ZanataTestCase {

    public final static int MAX_SHORT_TEST_DURATION = 180000;
    public final static int MAX_LONG_TEST_DURATION = 600000;

    @Rule
    public TestName testName = new TestName();

    /*
     * rhbz1096552 - disable test timeout for now
     * see https://bugzilla.redhat.com/show_bug.cgi?id=1096552
     * @Rule
     * public Timeout timeout = new Timeout(MAX_TEST_DURATION);
     */

    public DateTime testFunctionStart;

    private String getTestDescription() {
        return this.getClass().getCanonicalName()
                .concat(".")
                .concat(testName.getMethodName());
    }

    @Before
    public void testEntry() {
        log.info("Starting ".concat(getTestDescription()));
        testFunctionStart = new DateTime();
    }

    @After
    public void testExit() {
        Duration duration = new Duration(testFunctionStart, new DateTime());
        PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
                .appendLiteral("Finished "
                        .concat(getTestDescription()).concat(" in "))
                .printZeroAlways()
                .appendMinutes()
                .appendSuffix(" minutes, ")
                .appendSeconds()
                .appendSuffix(" seconds, ")
                .appendMillis()
                .appendSuffix("ms")
                .toFormatter();
        log.info(periodFormatter.print(duration.toPeriod()));
        WebDriverFactory.INSTANCE.logLogs();
    }

}
