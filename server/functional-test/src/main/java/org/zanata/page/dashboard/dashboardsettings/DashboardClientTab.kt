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
package org.zanata.page.dashboard.dashboardsettings

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.dashboard.DashboardBasePage

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Carlos Munoz [camunoz@redhat.com](mailto:camunoz@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class DashboardClientTab(driver: WebDriver) : DashboardBasePage(driver) {
    private val generateApiKeyButton = By.id("generate-key-button")
    private val apiKeyLabel = By.id("client_settings_apiKey")
    private val configurationTextArea = By.id("client_settings_config")
    private val alertModalOkButton = By.id("confirm-ok-button")

    val apiKey: String
        get() {
            log.info("Query API Key")
            return readyElement(apiKeyLabel).getAttribute("value")
        }

    val configurationDetails: String
        get() {
            log.info("Query configuration details")
            return readyElement(configurationTextArea).text
        }

    fun pressApiKeyGenerateButton(): DashboardClientTab {
        log.info("Press Generate API Key")
        clickElement(generateApiKeyButton)
        slightPause()
        clickElement(alertModalOkButton)
        return DashboardClientTab(driver)
    }

    fun expectApiKeyChanged(current: String) {
        log.info("Wait for API key changed from {}", current)
        waitForPageSilence()
        assertThat(apiKey).isNotEqualTo(current)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(DashboardClientTab::class.java)
    }
}
