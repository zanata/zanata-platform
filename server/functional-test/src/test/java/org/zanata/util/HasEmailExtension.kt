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

import java.net.BindException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.mail.internet.MimeMultipart

import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.subethamail.wiser.Wiser
import org.subethamail.wiser.WiserMessage
import com.google.common.base.Throwables
import com.google.common.util.concurrent.Uninterruptibles

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class HasEmailExtension : BeforeTestExecutionCallback, AfterTestExecutionCallback {

    val messages: List<WiserMessage>
        get() = wiser!!.messages

    @Throws(UnknownHostException::class)
    override fun beforeTestExecution(context: ExtensionContext) {
        synchronized(wiserLock) {
            if (HasEmailExtension.wiser == null) {
                val port = PropertiesHolder.getProperty("smtp.port")
                val portNum = Integer.parseInt(port)
                val w = Wiser(portNum)
                w.server.bindAddress = InetAddress.getByName("127.0.0.1")
                try {
                    w.start()
                } catch (e: RuntimeException) {
                    if (Throwables.getRootCause(e) is BindException) {
                        val processInfo = ProcessPortInfo.getPortProcessInfo(portNum)
                        log.error(
                                "The following process is already listening to port {}:\n{}",
                                portNum, processInfo)
                        throw RuntimeException("Error binding port "
                                + portNum + ". See log for more info.", e)
                    }
                    throw e
                }

                HasEmailExtension.wiser = w
                // NB we never call wiser.stop() because we want the email
                // server to stay running for all tests in this VM
            }
        }
        clearQueue()
    }

    override fun afterTestExecution(context: ExtensionContext) {
        clearQueue()
    }

    private fun clearQueue() {
        log.info("Clearing email queue")
        wiser!!.messages.clear()
    }

    /**
     * Email may arrive a little bit late therefore this method can be used to
     * poll and wait until timeout.
     *
     * @param expectedEmailNum
     * expected arriving email numbers
     * @param timeoutDuration
     * timeout duration
     * @param timeoutUnit
     * timeout time unit
     * @return true if the expected number of emails has arrived or false if it
     * fails within the timeout period
     */
    fun emailsArrivedWithinTimeout(expectedEmailNum: Int,
                                   timeoutDuration: Long, timeoutUnit: TimeUnit): Boolean {
        log.info("waiting for email count to be {}", expectedEmailNum)
        val countDownLatch = CountDownLatch(1)
        // poll every half second
        val sleepFor = 500
        val sleepUnit = TimeUnit.MILLISECONDS
        val sleepTime = sleepUnit.convert(sleepFor.toLong(), sleepUnit)
        val timeoutTime = sleepUnit.convert(timeoutDuration, timeoutUnit)
        val runnable = {
            var slept: Long = 0
            while (wiser!!.messages.size < expectedEmailNum && slept < timeoutTime) {
                log.info("Number of arrived emails: {}",
                        wiser!!.messages.size)
                Uninterruptibles.sleepUninterruptibly(sleepFor.toLong(), sleepUnit)
                slept += sleepTime
            }
            countDownLatch.countDown()
        }
        Executors.newFixedThreadPool(1).submit(runnable)
        return try {
            countDownLatch.await(timeoutDuration, timeoutUnit)
        } catch (e: InterruptedException) {
            log.warn("interrupted", e)
            wiser!!.messages.size == expectedEmailNum
        }

    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(HasEmailExtension::class.java)

        private val wiserLock = Any()
        @Volatile
        private var wiser: Wiser? = null

        fun getEmailContent(wiserMessage: WiserMessage): String {
            log.info("Query message content")
            try {
                return (wiserMessage.mimeMessage.content as MimeMultipart)
                        .getBodyPart(0).content.toString()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
    }
}
