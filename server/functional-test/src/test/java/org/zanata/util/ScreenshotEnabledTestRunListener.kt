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
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.zanata.page.WebDriverFactory

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class ScreenshotEnabledTestRunListener : RunListener() {

    @Volatile
    private var testFailed: Boolean = false

    @Throws(Exception::class)
    override fun testStarted(description: Description?) {
        super.testStarted(description)
        if (description!!.testClass.getAnnotation(NoScreenshot::class.java) == null
                && description.getAnnotation(NoScreenshot::class.java) == null) {
            enableScreenshotForTest(description.displayName)
        }
    }

    @Throws(Exception::class)
    override fun testFinished(description: Description?) {
        super.testFinished(description)
        unregisterScreenshot()
        if (!testFailed) {
            deleteScreenshots(description!!.displayName)
        }
    }

    @Throws(Exception::class)
    override fun testFailure(failure: Failure?) {
        super.testFailure(failure)
        testFailed = true
    }

    companion object {
        private val log = org.slf4j.LoggerFactory
                .getLogger(ScreenshotEnabledTestRunListener::class.java)

        @Throws(Exception::class)
        private fun enableScreenshotForTest(testDisplayName: String) {
            WebDriverFactory.INSTANCE.registerScreenshotListener(testDisplayName)
            val date = Date().toString()
            log.debug("[TEST] {}:{}", testDisplayName, date)
        }

        private fun deleteScreenshots(testDisplayName: String) {
            val testDir = screenshotForTest(testDisplayName)
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
