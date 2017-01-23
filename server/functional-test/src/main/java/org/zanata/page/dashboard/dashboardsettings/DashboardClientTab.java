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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.zanata.page.dashboard.DashboardBasePage;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DashboardClientTab extends DashboardBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DashboardClientTab.class);
    private By generateApiKeyButton = By.id("generateKeyButton");
    private By apiKeyLabel = By.id("client_settings_apiKey");
    private By configurationTextArea = By.id("client_settings_config");

    public DashboardClientTab(WebDriver driver) {
        super(driver);
    }

    public DashboardClientTab pressApiKeyGenerateButton() {
        log.info("Press Generate API Key");
        clickElement(generateApiKeyButton);
        slightPause();
        Alert alert = waitForAMoment().withMessage("Alert dialog not displayed")
                .until((Function<WebDriver, Alert>) webDriver -> {
                    return webDriver.switchTo().alert();
                });
        log.info("Press OK on alert to generate API key");
        alert.accept();
        waitForAMoment().withMessage("Alert not dismissed")
                .until((Predicate<WebDriver>) webDriver -> {
                    try {
                        getDriver().switchTo().alert();
                    } catch (NoAlertPresentException nape) {
                        return true;
                    }
                    log.info("Alert still present");
                    return false;
                });
        return new DashboardClientTab(getDriver());
    }

    public String getApiKey() {
        log.info("Query API Key");
        return readyElement(apiKeyLabel).getAttribute("value");
    }

    public String getConfigurationDetails() {
        log.info("Query configuration details");
        return readyElement(configurationTextArea).getText();
    }

    public void expectApiKeyChanged(final String current) {
        log.info("Wait for API key changed from {}", current);
        waitForPageSilence();
        assertThat(getApiKey()).isNotEqualTo(current);
    }
}
