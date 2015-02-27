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

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.zanata.page.BasePage;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ServerConfigurationPage extends BasePage {

    private By urlField = By.id("serverConfigForm:urlField");
    private By helpUrlField = By.id("serverConfigForm:helpUrlField");
    private By maxConcurrentField = By.id("serverConfigForm:maxConcurrentPerApiKeyField:maxConcurrentPerApiKeyEml");
    private By maxActiveField = By.id("serverConfigForm:maxActiveRequestsPerApiKeyField:maxActiveRequestsPerApiKeyEml");
    private By saveButton = By.id("serverConfigForm:save");

    public ServerConfigurationPage(WebDriver driver) {
        super(driver);
    }

    public ServerConfigurationPage inputHelpURL(String url) {
        log.info("Enter Help URL {}", url);
        scrollIntoView(waitForWebElement(helpUrlField));
        new Actions(getDriver()).moveToElement(waitForWebElement(helpUrlField))
                .click().sendKeys(url).perform();
        return new ServerConfigurationPage(getDriver());
    }

    public ServerConfigurationPage inputMaxConcurrent(int max) {
        log.info("Enter maximum concurrent API requests {}", max);
        waitForWebElement(maxConcurrentField).clear();
        waitForWebElement(maxConcurrentField).sendKeys(max + "");
        return new ServerConfigurationPage(getDriver());
    }

    public String getMaxConcurrentRequestsPerApiKey() {
        log.info("Query maximum concurrent API requests");
        return waitForWebElement(maxConcurrentField).getAttribute("value");
    }

    public ServerConfigurationPage inputMaxActive(int max) {
        log.info("Enter maximum active API requests {}", max);
        waitForWebElement(maxActiveField).clear();
        waitForWebElement(maxActiveField).sendKeys(max + "");
        return new ServerConfigurationPage(getDriver());
    }

    public String getMaxActiveRequestsPerApiKey() {
        log.info("Query maximum active API requests");
        return waitForWebElement(maxActiveField).getAttribute("value");
    }

    public AdministrationPage save() {
        log.info("Click Save");
        waitForWebElement(saveButton).click();
        return new AdministrationPage(getDriver());
    }
}
