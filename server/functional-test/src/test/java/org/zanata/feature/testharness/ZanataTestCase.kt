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
package org.zanata.feature.testharness

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension
import org.zanata.page.WebDriverFactory
import org.zanata.util.AllowAnonymousExtension
import org.zanata.util.LoggingExtension
import org.zanata.util.HasEmailExtension
import org.zanata.util.EnsureLogoutExtension
import org.zanata.util.SampleDataExtension
import org.zanata.util.ZanataRestCaller

/**
 * Global application of rules to Zanata functional tests
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@ExtendWith(LoggingExtension::class,
        AllowAnonymousExtension::class,
        EnsureLogoutExtension::class,
        SampleDataExtension::class)
@DisplayName("Zanata Functional Test")
open class ZanataTestCase {

    // Alternative method
    // @JvmField
    // @RegisterExtension
    // val sampleDataExtension = SampleDataExtension()

    private var testFunctionStart = DateTime()
    var zanataRestCaller = ZanataRestCaller()

    private val testDescription: String
        get() = this.javaClass.canonicalName

    @BeforeEach
    fun testEntry() {
        WebDriverFactory.INSTANCE.registerLogListener()
        log.info("Test starting: {}", testDescription)
        testFunctionStart = DateTime()
        WebDriverFactory.INSTANCE.testEntry()
        zanataRestCaller = ZanataRestCaller()
        zanataRestCaller.signalBeforeTest(javaClass.name, "null")
        mainWindowHandle = WebDriverFactory.INSTANCE.driver.windowHandle
    }

    @AfterEach
    fun testExit() {
        WebDriverFactory.INSTANCE.logLogs()
        zanataRestCaller.signalAfterTest(javaClass.name, "null")
        val duration = Duration(testFunctionStart, DateTime())
        val periodFormatter = PeriodFormatterBuilder()
                .appendLiteral("Test finished: $testDescription: in ")
                .printZeroAlways().appendMinutes().appendSuffix(" minutes, ")
                .appendSeconds().appendSuffix(" seconds, ").appendMillis()
                .appendSuffix("ms").toFormatter()
        log.info(periodFormatter.print(duration.toPeriod()))
        cleanUpWindows()
        WebDriverFactory.INSTANCE.testExit()
    }

    // Close all windows other than the original one
    private fun cleanUpWindows() {

        val driverRef = WebDriverFactory.INSTANCE.driver
        val windows = driverRef.windowHandles
        for (windowHandle in windows) {
            if (windowHandle != mainWindowHandle) {
                log.info("Closing {}", windowHandle)
                driverRef.switchTo().window(windowHandle)
                driverRef.close()
            }
        }
        driverRef.switchTo().window(mainWindowHandle)
        WebDriverFactory.INSTANCE.unregisterLogListener()
        // uncomment this if you need a fresh browser between test runs
        // WebDriverFactory.INSTANCE.killWebDriver();
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ZanataTestCase::class.java)

        @RegisterExtension
        var hasEmailExtension = HasEmailExtension()
        private lateinit var mainWindowHandle: String
    }
}
