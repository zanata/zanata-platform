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

import org.apache.commons.io.FileUtils
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.slf4j.LoggerFactory.getLogger
import org.zanata.page.WebDriverFactory
import java.io.IOException
import java.util.Date

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class ScreenshotEnabledTestExecutionListener : TestExecutionListener {

    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (!testIdentifier.type.isTest) return
        val name = getName(testIdentifier)
        if (name != null) {
            enableScreenshotForTest(name)
        } else {
            log.warn("Unable to enable screenshots for {}", testIdentifier.uniqueId)
        }
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        unregisterScreenshot()
        if (testExecutionResult.status !== TestExecutionResult.Status.FAILED) {
            getName(testIdentifier)?.let { deleteScreenshots(it) }
        }
    }

    private fun getName(testIdentifier: TestIdentifier): String? {
        val testMethod = findTestMethod(testIdentifier)
        return if (testMethod.isPresent) {
            val method = testMethod.get()
            if (method.javaClass.isAnnotationPresent(NoScreenshot::class.java) ||
                    method.isAnnotationPresent(NoScreenshot::class.java)) {
                log.debug("Skipping screenshots for @NoScreenshot test {}", testIdentifier.uniqueId)
                null
            } else {
                getQualifiedName(method)
            }
        } else null
    }

    companion object {
        private val log = getLogger(ScreenshotEnabledTestExecutionListener::class.java)

        private fun enableScreenshotForTest(name: String) {
            WebDriverFactory.INSTANCE.registerScreenshotListener(name)
            val date = Date().toString()
            log.debug("[TEST] {}:{}", name, date)
        }

        private fun deleteScreenshots(name: String) {
            val testDir = screenshotForTest(name)
            try {
                log.info("Deleting screenshots for {}", name)
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
