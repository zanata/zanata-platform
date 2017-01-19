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
package org.zanata.page.administration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ServerConfigurationPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ServerConfigurationPage.class);
    private By urlField = By.id("serverConfigForm:url:input:url");
    public static final By registerUrlField =
            By.id("serverConfigForm:register:input:registerUrl");
    private By emailDomainField =
            By.id("serverConfigForm:emailDomainField:emailDomain");
    private By adminEmailField =
            By.id("serverConfigForm:adminEmail:input:adminEml");
    public static final By fromEmailField =
            By.id("serverConfigForm:fromEmail:input:fromEml");
    private By enableLogCheck = By.id("serverConfigForm:enableLogCheck");
    private By logLevelSelect = By.id("serverConfigForm:logEmailLvl");
    private By emailDestinationField =
            By.id("serverConfigForm:logDestEmail:input:logDestEml");
    private By helpUrlField = By.id("serverConfigForm:helpUrl:input:helpInput");
    private By termsUrlField =
            By.id("serverConfigForm:termsOfUseUrl:input:termsOfUseUrlEml");
    private By piwikUrl = By.id("serverConfigForm:piwikUrl:input:piwikUrlEml");
    private By piwikId = By.id("serverConfigForm:piwikIdSiteEml");
    private By maxConcurrentField = By.id(
            "serverConfigForm:maxConcurrentPerApiKey:input:maxConcurrentPerApiKeyEml");
    private By maxActiveField = By.id(
            "serverConfigForm:maxActiveRequestsPerApiKey:input:maxActiveRequestsPerApiKeyEml");
    private By saveButton = By.id("serverConfigForm:save");

    public ServerConfigurationPage(WebDriver driver) {
        super(driver);
    }

    private void enterTextConfigField(By by, String text) {
        touchTextField(readyElement(by));
        enterText(readyElement(by), text);
        expectFieldValue(by, text);
    }

    public ServerConfigurationPage inputServerURL(String url) {
        log.info("Enter Server URL {}", url);
        enterTextConfigField(urlField, url);
        return new ServerConfigurationPage(getDriver());
    }

    public ServerConfigurationPage inputRegisterURL(String url) {
        log.info("Enter Register URL {}", url);
        enterTextConfigField(registerUrlField, url);
        return new ServerConfigurationPage(getDriver());
    }

    public boolean expectFieldValue(final By by, final String expectedValue) {
        log.info("Wait for field {} value {}", by.toString(), expectedValue);
        return waitForAMoment().withMessage("text present: " + by.toString())
                .until(ExpectedConditions.textToBePresentInElementValue(
                        existingElement(by), expectedValue));
    }

    public ServerConfigurationPage inputAdminEmail(String email) {
        log.info("Enter admin email address {}", email);
        enterTextConfigField(adminEmailField, email);
        return new ServerConfigurationPage(getDriver());
    }

    public ServerConfigurationPage inputAdminFromEmail(String email) {
        log.info("Enter admin From email address {}", email);
        enterTextConfigField(fromEmailField, email);
        return new ServerConfigurationPage(getDriver());
    }

    public ServerConfigurationPage inputHelpURL(String url) {
        log.info("Enter Help URL {}", url);
        enterTextConfigField(helpUrlField, url);
        return new ServerConfigurationPage(getDriver());
    }

    public ServerConfigurationPage inputTermsOfUseURL(String url) {
        log.info("Enter Terms of Use URL {}", url);
        enterTextConfigField(termsUrlField, url);
        return new ServerConfigurationPage(getDriver());
    }

    public ServerConfigurationPage clickLoggingEnabledCheckbox() {
        log.info("Click enable logging checkbox");
        clickElement(enableLogCheck);
        return new ServerConfigurationPage(getDriver());
    }

    public ServerConfigurationPage selectLoggingLevel(String logLevel) {
        log.info("Select logging level {}", logLevel);
        new Select(readyElement(logLevelSelect)).selectByVisibleText(logLevel);
        return new ServerConfigurationPage(getDriver());
    }

    public String selectedLoggingLevel() {
        log.info("Query selected logging level");
        return new Select(readyElement(logLevelSelect)).getFirstSelectedOption()
                .getText();
    }

    public ServerConfigurationPage inputLogEmailTarget(String email) {
        log.info("Enter log email target {}", email);
        enterTextConfigField(emailDestinationField, email);
        return new ServerConfigurationPage(getDriver());
    }

    public String getLogEmailTarget() {
        log.info("Query log email target");
        return readyElement(emailDestinationField).getAttribute("value");
    }

    public ServerConfigurationPage inputPiwikUrl(String url) {
        log.info("Enter Piwik URL", url);
        enterTextConfigField(piwikUrl, url);
        return new ServerConfigurationPage(getDriver());
    }

    public String getPiwikUrl() {
        log.info("Query Piwik URL");
        return readyElement(piwikUrl).getAttribute("value");
    }

    public ServerConfigurationPage inputPiwikID(String id) {
        log.info("Enter Piwik ID", id);
        enterTextConfigField(piwikId, id);
        return new ServerConfigurationPage(getDriver());
    }

    public String getPiwikID() {
        log.info("Query Piwik ID");
        return readyElement(piwikId).getAttribute("value");
    }

    public ServerConfigurationPage inputMaxConcurrent(int max) {
        log.info("Enter maximum concurrent API requests {}", max);
        readyElement(maxConcurrentField).clear();
        enterText(readyElement(maxConcurrentField), max + "");
        return new ServerConfigurationPage(getDriver());
    }

    public String getMaxConcurrentRequestsPerApiKey() {
        log.info("Query maximum concurrent API requests");
        return readyElement(maxConcurrentField).getAttribute("value");
    }

    public ServerConfigurationPage inputMaxActive(int max) {
        log.info("Enter maximum active API requests {}", max);
        readyElement(maxActiveField).clear();
        enterText(readyElement(maxActiveField), max + "");
        return new ServerConfigurationPage(getDriver());
    }

    public String getMaxActiveRequestsPerApiKey() {
        log.info("Query maximum active API requests");
        return readyElement(maxActiveField).getAttribute("value");
    }

    public AdministrationPage save() {
        log.info("Click Save");
        clickElement(saveButton);
        return new AdministrationPage(getDriver());
    }
}
