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

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.zanata.page.WebDriverFactory;
import org.zanata.util.AllowAnonymousAccessRule;
import org.zanata.util.EnsureLogoutRule;
import org.zanata.util.SampleDataResourceClient;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;

/**
 * Global application of rules to Zanata functional tests
 *
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ZanataTestCase.class);

    public static final int MAX_SHORT_TEST_DURATION = 180000;
    public static final int MAX_LONG_TEST_DURATION = 600000;
    @ClassRule
    public static ExternalResource javascriptLogging = new ExternalResource() {

        @Override
        protected void before() throws Throwable {
            WebDriverFactory.INSTANCE.registerLogListener();
        }

        @Override
        protected void after() {
            WebDriverFactory.INSTANCE.unregisterLogListener();
            // uncomment this if you need a fresh browser between test runs
            // WebDriverFactory.INSTANCE.killWebDriver();
        }
    };
    @Rule
    public final TestName testName = new TestName();
    @Rule
    public RuleChain theOneRule = RuleChain
            .outerRule(new AllowAnonymousAccessRule())
            .around(new EnsureLogoutRule()).around(new SampleProjectRule());
    /*
     * rhbz1096552 - disable test timeout for now see
     * https://bugzilla.redhat.com/show_bug.cgi?id=1096552
     *
     * @Rule public Timeout timeout = new Timeout(MAX_TEST_DURATION);
     */
    public DateTime testFunctionStart;
    private ZanataRestCaller zanataRestCaller = new ZanataRestCaller();

    private String getTestDescription() {
        return this.getClass().getCanonicalName().concat(".")
                .concat(testName.getMethodName());
    }

    @Before
    public final void testEntry() {
        log.info("Test starting: {}", getTestDescription());
        testFunctionStart = new DateTime();
        WebDriverFactory.INSTANCE.testEntry();
        zanataRestCaller.signalBeforeTest(getClass().getName(),
                testName.getMethodName());
    }

    @After
    public final void testExit() {
        WebDriverFactory.INSTANCE.logLogs();
        zanataRestCaller.signalAfterTest(getClass().getName(),
                testName.getMethodName());
        Duration duration = new Duration(testFunctionStart, new DateTime());
        PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
                .appendLiteral("Test finished: ".concat(getTestDescription())
                        .concat(": in "))
                .printZeroAlways().appendMinutes().appendSuffix(" minutes, ")
                .appendSeconds().appendSuffix(" seconds, ").appendMillis()
                .appendSuffix("ms").toFormatter();
        log.info(periodFormatter.print(duration.toPeriod()));
        WebDriverFactory.INSTANCE.testExit();
    }
}
