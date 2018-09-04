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

import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import com.google.common.base.Throwables.getRootCause

/**
 * @author Sean Flanigan
 * [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
class TestLogger : RunListener() {

    @Throws(Exception::class)
    override fun testStarted(description: Description?) {
        log.info("Test starting: {}", description)
    }

    @Throws(Exception::class)
    override fun testFinished(description: Description?) {
        log.info("Test finished: {}", description)
    }

    @Throws(Exception::class)
    override fun testIgnored(description: Description?) {
        log.error("Test IGNORED: {}", description)
    }

    @Throws(Exception::class)
    override fun testFailure(failure: Failure?) {
        val e = failure!!.exception
        log.error("Test FAILED: $failure", getRootCause(e))
    }

    override fun testAssumptionFailure(failure: Failure?) {
        log.error("Test FAILED ASSUMPTION: " + failure!!, failure.exception)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(TestLogger::class.java)
    }
}
