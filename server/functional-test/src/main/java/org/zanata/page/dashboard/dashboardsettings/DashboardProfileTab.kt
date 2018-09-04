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

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:camunoz@redhat.com)
 */
class DashboardProfileTab(driver: WebDriver) : DashboardBasePage(driver) {
    private val accountNameField = By.id("profileForm:nameField:input:accountName")
    private val updateProfileButton = By.id("updateProfileButton")

    val username: String
        get() {
            log.info("Query user name")
            return readyElement(By.id("profileForm"))
                    .findElement(By.className("l--push-bottom-0")).text
        }

    fun enterName(name: String): DashboardProfileTab {
        log.info("Enter name {}", name)
        readyElement(accountNameField).clear()
        enterText(readyElement(accountNameField), name)
        return DashboardProfileTab(driver)
    }

    fun clickUpdateProfileButton(): DashboardProfileTab {
        log.info("Click Update")
        clickElement(updateProfileButton)
        return DashboardProfileTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(DashboardProfileTab::class.java)
    }
}
