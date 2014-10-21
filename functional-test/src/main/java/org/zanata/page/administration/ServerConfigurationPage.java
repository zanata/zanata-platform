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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ServerConfigurationPage extends BasePage {
    @FindBy(id = "serverConfigForm:urlField")
    private WebElement urlField;

    @FindBy(
            id = "serverConfigForm:maxConcurrentPerApiKeyField:maxConcurrentPerApiKeyEml")
    private WebElement maxConcurrentField;

    @FindBy(
            id = "serverConfigForm:maxActiveRequestsPerApiKeyField:maxActiveRequestsPerApiKeyEml")
    private WebElement maxActiveField;

    @FindBy(id = "serverConfigForm:save")
    private WebElement saveButton;

    public ServerConfigurationPage(WebDriver driver) {
        super(driver);
    }

    public ServerConfigurationPage inputMaxConcurrent(int max) {
        log.info("Enter maximum concurrent API requests {}", max);
        maxConcurrentField.clear();
        maxConcurrentField.sendKeys(max + "");
        return this;
    }

    public String getMaxConcurrentRequestsPerApiKey() {
        log.info("Query maximum concurrent API requests");
        return maxConcurrentField.getAttribute("value");
    }

    public ServerConfigurationPage inputMaxActive(int max) {
        log.info("Enter maximum active API requests {}", max);
        maxActiveField.clear();
        maxActiveField.sendKeys(max + "");
        return this;
    }

    public String getMaxActiveRequestsPerApiKey() {
        log.info("Query maximum active API requests");
        return maxActiveField.getAttribute("value");
    }

    public AdministrationPage save() {
        log.info("Click Save");
        saveButton.click();
        return new AdministrationPage(getDriver());
    }
}
