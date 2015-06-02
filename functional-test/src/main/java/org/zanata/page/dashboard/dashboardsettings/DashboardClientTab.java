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

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.dashboard.DashboardBasePage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class DashboardClientTab extends DashboardBasePage {

    private By generateApiKeyButton = By.id("generateKeyButton");
    private By apiKeyLabel = By.id("apiKey");
    private By configurationTextArea = By.id("config");

    public DashboardClientTab(WebDriver driver) {
        super(driver);
    }

    public DashboardClientTab pressApiKeyGenerateButton() {
        log.info("Press Generate API Key");
        clickElement(generateApiKeyButton);
        getDriver().switchTo().alert().accept();
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
