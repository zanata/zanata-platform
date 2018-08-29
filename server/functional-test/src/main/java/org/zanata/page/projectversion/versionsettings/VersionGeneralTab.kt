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
package org.zanata.page.projectversion.versionsettings

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.projectversion.VersionBasePage
import org.zanata.page.projectversion.VersionLanguagesPage

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class VersionGeneralTab(driver: WebDriver) : VersionBasePage(driver) {
    private val versionIdField = By.id("settings-general_form:slug:input:slug")
    private val updateButton = By.id("settings-general_form:updateButton")

    val versionID: String
        get() = readyElement(versionIdField).getAttribute("value")

    fun enterVersionID(newSlug: String): VersionGeneralTab {
        log.info("Enter project version slug {}", newSlug)
        readyElement(versionIdField).clear()
        enterText(readyElement(versionIdField), newSlug)
        defocus(versionIdField)
        return VersionGeneralTab(driver)
    }

    fun updateVersion(): VersionLanguagesPage {
        log.info("Click Update general settings")
        scrollIntoView(readyElement(updateButton))
        clickAndCheckErrors(readyElement(updateButton))
        return VersionLanguagesPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(VersionGeneralTab::class.java)
    }
}
