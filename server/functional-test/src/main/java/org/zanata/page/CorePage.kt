/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page

import java.util.ArrayList
import org.apache.commons.lang3.StringUtils
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.more.MorePage
import org.zanata.page.utility.HomePage
import org.zanata.util.WebElementUtil
import com.google.common.base.Joiner
import kotlin.streams.toList

/**
 * Contains the physical elements, such as page title and home link, that must
 * exist on all Zanata pages.
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
open class CorePage(driver: WebDriver) : AbstractPage(driver) {
    private val homeLink = By.id("nav_home")
    private val moreLink = By.id("nav_more")

    val title: String
        get() = driver.title

    // app-error is a pseudo class we put in just for this
    val errors: List<String>
        get() {
            log.info("Query page errors")
            val errorStrings = mutableListOf<String>()
            errorStrings.addAll(
                    WebElementUtil.elementsToText(driver,
                    By.xpath("//span[@class=\'errors\']"))
                    .stream().filter { s -> StringUtils.isNotBlank(s) }.toList())
            errorStrings.addAll(WebElementUtil.elementsToText(driver,
                    By.className("app-error"))
                    .stream().filter { s -> StringUtils.isNotBlank(s) }.toList())
            return errorStrings
        }

    val notificationMessage: String
        get() = getNotificationMessage(By.id("messages"))

    init {
        assertNoCriticalErrors()
    }

    fun goToHomePage(): HomePage {
        log.info("Click Zanata home icon")
        scrollToTop()
        clickElement(homeLink)
        return HomePage(driver)
    }

    fun gotoMorePage(): MorePage {
        log.info("Click More icon")
        clickElement(moreLink)
        return MorePage(driver)
    }

    protected fun clickAndCheckErrors(button: WebElement) {
        clickElement(button)
        val errors = errors
        if (!errors.isEmpty()) {
            throw RuntimeException(Joiner.on(";").join(errors))
        }
    }

    protected fun clickAndExpectErrors(button: WebElement) {
        clickElement(button)
        refreshPageUntil(this, "errors exist") { errors.isNotEmpty() }
    }

    /**
     * Wait until expected number of errors presented on page or timeout.
     *
     * @param expectedNumber
     * expected number of errors on page
     * @return list of error message
     */
    fun getErrors(expectedNumber: Int): List<String> {
        log.info("Query page errors, expecting {}", expectedNumber)
        refreshPageUntil(this, "errors = $expectedNumber") {
            errors.size == expectedNumber
        }
        return errors
    }

    /**
     * Wait until an expected error is visible
     *
     * @param expected
     * The expected error string
     * @return The full list of visible errors
     */
    fun expectError(expected: String): List<String> {
        val msg = "expected error: $expected"
        logWaiting(msg)
        waitForAMoment().withMessage(msg).until { errors.contains(expected) }
        logFinished(msg)
        return errors
    }

    fun getNotificationMessage(elementBy: By): String {
        log.info("Query notification message")
        val messages = getNotificationMessages(elementBy)
        return if (messages.isNotEmpty()) messages[0].text else ""
    }

    private fun getNotificationMessages(inputElement: By): List<WebElement> {
        return existingElement(inputElement).findElements(By.tagName("li"))
    }

    fun expectNotification(notification: String): Boolean {
        val msg = "Expecting notification $notification"
        logWaiting(msg)
        val result = waitForAMoment().withMessage(msg).until {
            val messages = getNotificationMessages(By.id("messages"))
            println("Found " + messages.size + " messages")
            val notifications = ArrayList<String>()
            for (message in messages) {
                println(message.text.trim())
                notifications.add(message.text.trim())
            }
            if (!notifications.isEmpty()) {
                triggerScreenshot("_notify")
                log.info("Notifications: {}", notifications)
            }
            notifications.contains(notification)
        }
        logFinished(msg)
        return result
    }

    fun assertNoCriticalErrors() {
        val errors = driver.findElements(By.className("alert--danger"))
        if (errors.size > 0) {
            log.info("Error page displayed")
            throw RuntimeException(
                    "Critical error: \n" + errors[0].text)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(CorePage::class.java)


        val h3Header = By.tagName("h3")!!
        val paragraph = By.tagName("p")!!
        val inputElement = By.tagName("input")!!
        val tableElement = By.tagName("table")!!
    }
}
