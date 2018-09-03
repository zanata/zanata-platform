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
package org.zanata.util

import java.io.IOException
import java.util.Date
import org.apache.commons.io.FileUtils
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.slf4j.LoggerFactory.getLogger
import org.zanata.page.WebDriverFactory

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class ScreenshotEnabledTestRunListener : TestExecutionListener {

    override fun executionStarted(testIdentifier: TestIdentifier) {
        // FIXME we can't get the test class or method easily
        // but there are some ideas here:
        // https://stackoverflow.com/questions/42781020/junit5-is-there-any-reliable-way-to-get-class-of-an-executed-test
        // and https://github.com/junit-team/junit5/issues/737

        // NB We don't have access to the test class in a TestExecutionListener
        // (to check annotations like NoScreenshot)
        // (The closest thing is testIdentifier.legacyReportingName which _may_
        // start with a class name)
        enableScreenshotForTest(testIdentifier.displayName)
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        unregisterScreenshot()
        if (testExecutionResult.status == TestExecutionResult.Status.FAILED) {
            deleteScreenshots(testIdentifier.displayName)
        }
    }

    companion object {
        private val log = getLogger(ScreenshotEnabledTestRunListener::class.java)

        private fun enableScreenshotForTest(testDisplayName: String) {
            WebDriverFactory.INSTANCE.registerScreenshotListener(testDisplayName)
            val date = Date().toString()
            log.debug("[TEST] {}:{}", testDisplayName, date)
        }

        private fun deleteScreenshots(testDisplayName: String) {
            val testDir = ScreenshotDirForTest.screenshotForTest(testDisplayName)
            try {
                log.info("Deleting screenshots for {}", testDisplayName)
                FileUtils.deleteDirectory(testDir)
            } catch (e: IOException) {
                log.warn("error deleting screenshot base directory: {}",
                        e.message)
            }
        }

        private fun unregisterScreenshot() {
            WebDriverFactory.INSTANCE.unregisterScreenshotListener()
        }
    }
}
