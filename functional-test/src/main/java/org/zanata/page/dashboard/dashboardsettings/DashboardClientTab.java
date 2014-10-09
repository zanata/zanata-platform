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
package org.zanata.page.dashboard.dashboardsettings;

import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.dashboard.DashboardBasePage;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class DashboardClientTab extends DashboardBasePage {

    @FindBy(id = "generateKeyButton")
    private WebElement generateApiKeyButton;

    @FindBy(id = "apiKey")
    private WebElement apiKeyLabel;

    @FindBy(id = "config")
    private WebElement configurationTextArea;

    public DashboardClientTab(WebDriver driver) {
        super(driver);
    }

    public DashboardClientTab pressApiKeyGenerateButton() {
        log.info("Press Generate API Key");
        generateApiKeyButton.click();
        getDriver().switchTo().alert().accept();
        return new DashboardClientTab(getDriver());
    }

    public String getApiKey() {
        log.info("Query API Key");
        return apiKeyLabel.getAttribute("value");
    }

    public String getConfigurationDetails() {
        log.info("Query configuration details");
        return configurationTextArea.getText();
    }

    public void waitForApiKeyChanged(final String current) {
        log.info("Wait for API key changed from {}", current);
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !getApiKey().equals(current);
            }
        });
    }
}
