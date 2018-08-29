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
package org.zanata.page.administration

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.zanata.page.BasePage


/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class ServerConfigurationPage(driver: WebDriver) : BasePage(driver) {

    private val urlField = By.id("host.url")
    private val adminEmailField = By.id("email.admin.addr")
    private val enableLogCheck = By.id("log.email.active")
    private val logLevelSelect = By.id("log.email.level")
    private val emailDestinationField = By.id("log.destination.email")
    private val helpUrlField = By.id("help.url")
    private val termsUrlField = By.id("terms.conditions.url")
    private val piwikUrlField = By.id("piwik.url")
    private val piwikId = By.id("piwik.idSite")
    private val maxConcurrentField = By.id("max.concurrent.req.per.apikey")
    private val maxActiveField = By.id("max.active.req.per.apikey")
    private val saveButton = By.xpath("//button[contains(.,'Save')]")

    /**
     * Query the value of the log email field
     * @return new ServerConfigurationPage
     */
    val logEmailTarget: String
        get() {
            log.info("Query log email target")
            return getAttribute(emailDestinationField, "value")
        }

    /**
     * Query the value of the piwik ID field
     * @return String of value in piwik ID field
     */
    val piwikID: String
        get() {
            log.info("Query Piwik ID")
            return getAttribute(piwikId, "value")
        }

    /**
     * Retrieve the value from the max concurrent requests field
     * @return String of value in max concurrent requests field
     */
    val maxConcurrentRequestsPerApiKey: String
        get() {
            log.info("Query maximum concurrent API requests")
            return getAttribute(maxConcurrentField, "value")
        }

    /**
     * Retrieve the value from the maximum active requests field
     * @return String of maximum active requests value
     */
    val maxActiveRequestsPerApiKey: String
        get() {
            log.info("Query maximum active API requests")
            return getAttribute(maxActiveField, "value")
        }

    // Ensure text is entered into the config fields
    private fun enterTextConfigField(by: By, text: String) {
        touchTextField(readyElement(by))
        enterText(readyElement(by), text)
        expectFieldValue(by, text)
    }

    /**
     * Wait until the field contains the expected text
     * @param by locator of field
     * @param expectedValue text to compare actual value to
     */
    fun expectFieldValue(by: By, expectedValue: String) {
        log.info("Wait for field {} value {}", by.toString(), expectedValue)
        waitForAMoment().withMessage("text present: " + by.toString())
                .until(ExpectedConditions.textToBePresentInElementValue(
                        existingElement(by), expectedValue))
    }

    /**
     * Enter text into the server url field
     * @param url text to enter
     * @return new ServerConfigurationPage
     */
    fun inputServerURL(url: String): ServerConfigurationPage {
        log.info("Enter Server URL {}", url)
        enterTextConfigField(urlField, url)
        return ServerConfigurationPage(driver)
    }

    /**
     * Enter text into the registration url field
     * @param url text to enter
     * @return new ServerConfigurationPage
     */
    fun inputRegisterURL(url: String): ServerConfigurationPage {
        log.info("Enter Register URL {}", url)
        enterTextConfigField(registerUrlField, url)
        return ServerConfigurationPage(driver)
    }

    /**
     * Enter text into the admin email field
     * @param email text to enter
     * @return new ServerConfigurationPage
     */
    fun inputAdminEmail(email: String): ServerConfigurationPage {
        log.info("Enter admin email address {}", email)
        enterTextConfigField(adminEmailField, email)
        return ServerConfigurationPage(driver)
    }

    /**
     * Enter text into the admin 'from' email field
     * @param email text to enter
     * @return new ServerConfigurationPage
     */
    fun inputAdminFromEmail(email: String): ServerConfigurationPage {
        log.info("Enter admin From email address {}", email)
        enterTextConfigField(fromEmailField, email)
        return ServerConfigurationPage(driver)
    }

    /**
     * Enter text into the help url field
     * @param url text to enter
     * @return new ServerConfigurationPage
     */
    fun inputHelpURL(url: String): ServerConfigurationPage {
        log.info("Enter Help URL {}", url)
        enterTextConfigField(helpUrlField, url)
        return ServerConfigurationPage(driver)
    }

    /**
     * Enter text into the terms of use url field
     * @param url text to enter
     * @return new ServerConfigurationPage
     */
    fun inputTermsOfUseURL(url: String): ServerConfigurationPage {
        log.info("Enter Terms of Use URL {}", url)
        enterTextConfigField(termsUrlField, url)
        return ServerConfigurationPage(driver)
    }

    /**
     * Click to select or deselect the logging enabled checkbox
     * @return new ServerConfigurationPage
     */
    fun clickLoggingEnabledCheckbox(): ServerConfigurationPage {
        log.info("Click enable logging checkbox")
        driver.findElement(enableLogCheck).click()
        return ServerConfigurationPage(driver)
    }

    /**
     * Select a given log level
     * @param logLevel log level value to select
     * @return new ServerConfigurationPage
     */
    fun selectLoggingLevel(logLevel: String): ServerConfigurationPage {
        log.info("Select logging level {}", logLevel)

        driver.findElement(logLevelSelect).click()
        val option = driver
                .findElements(By.className("ant-select-dropdown-menu-item"))
                .stream().filter { e -> e.text == logLevel }.findFirst()

        if (option.isPresent) {
            option.get().click()
        } else {
            log.info("Cannot find option with log level {}", logLevel)
        }
        return ServerConfigurationPage(driver)
    }

    /**
     * Query the currently set log level
     * @return log level string
     */
    fun selectedLoggingLevel(): String {
        log.info("Query selected logging level")
        return readyElement(logLevelSelect).findElement(
                By.className("ant-select-selection-selected-value")).text
    }

    /**
     * Enter text into the log email field
     * @param email text to enter
     * @return new ServerConfigurationPage
     */
    fun inputLogEmailTarget(email: String): ServerConfigurationPage {
        log.info("Enter log email target {}", email)
        enterTextConfigField(emailDestinationField, email)
        return ServerConfigurationPage(driver)
    }

    /**
     * Enter text into the piwik url field
     * @param url text to enter
     * @return new ServerConfigurationPage
     */
    fun inputPiwikUrl(url: String): ServerConfigurationPage {
        log.info("Enter Piwik URL: {}", url)
        enterTextConfigField(piwikUrlField, url)
        return ServerConfigurationPage(driver)
    }

    /**
     * Query the value of the piwik url field
     * @return new ServerConfigurationPage
     */
    fun getPiwikUrl(): String {
        log.info("Query Piwik URL")
        return getAttribute(piwikUrlField, "value")
    }

    /**
     * Enter text into the piwik ID field
     * @param id text to enter
     * @return new ServerConfigurationPage
     */
    fun inputPiwikID(id: String): ServerConfigurationPage {
        log.info("Enter Piwik ID: {}", id)
        enterTextConfigField(piwikId, id)
        return ServerConfigurationPage(driver)
    }

    /**
     * Enter a value into the maximum concurrent requests field
     * @param max number to enter
     * @return new ServerConfigurationPage
     */
    fun inputMaxConcurrent(max: Int): ServerConfigurationPage {
        log.info("Enter maximum concurrent API requests {}", max)
        readyElement(maxConcurrentField).clear()
        enterText(maxConcurrentField, max.toString())
        return ServerConfigurationPage(driver)
    }

    /**
     * Enter a value into the maximum active requests field
     * @param max value to enter
     * @return new ServerConfigurationPage
     */
    fun inputMaxActive(max: Int): ServerConfigurationPage {
        log.info("Enter maximum active API requests {}", max)
        readyElement(maxActiveField).clear()
        enterText(maxActiveField, max.toString())
        return ServerConfigurationPage(driver)
    }

    /**
     * Press the Save button
     * @return new AdministrationPage
     */
    fun save(): ServerConfigurationPage {
        log.info("Click Save")
        clickElement(saveButton)
        return ServerConfigurationPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ServerConfigurationPage::class.java)
        val registerUrlField = By.id("register.url")!!
        val fromEmailField = By.id("email.from.addr")!!
    }
}
