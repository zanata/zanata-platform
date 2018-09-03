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

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.slf4j.LoggerFactory.getLogger

/**
 * @author Sean Flanigan
 * [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 * @see org.junit.platform.launcher.listeners.LoggingListener
 */
class TestLogger : TestExecutionListener {

    override fun executionStarted(testIdentifier: TestIdentifier) {
        super.executionStarted(testIdentifier)
        log.info("Test starting: {}", testIdentifier)
    }

    @Throws(Exception::class)
    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        val status = testExecutionResult.status
        log.info("Test finished ({}): {}", status, testIdentifier)
    }

    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String?) {
        log.error("Test skipped ({}): {}", reason, testIdentifier)
    }

    companion object {
        private val log = getLogger(TestLogger::class.java)
    }
}
