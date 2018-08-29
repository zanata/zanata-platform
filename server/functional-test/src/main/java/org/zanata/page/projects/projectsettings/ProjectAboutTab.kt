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
package org.zanata.page.projects.projectsettings

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.projects.ProjectBasePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ProjectAboutTab(driver: WebDriver) : ProjectBasePage(driver) {
    private val aboutTextField = By.id("settings-about-form:homeContent")
    private val saveButton = By.linkText("Save notes")

    val aboutText: String
        get() {
            log.info("Query About text")
            return readyElement(aboutTextField).text
        }

    fun enterAboutText(aboutText: String): ProjectAboutTab {
        log.info("Enter About text {}", aboutText)
        enterText(readyElement(aboutTextField), aboutText)
        return ProjectAboutTab(driver)
    }

    fun clearAboutText(): ProjectAboutTab {
        log.info("Clear About textedit")
        readyElement(aboutTextField).clear()
        return ProjectAboutTab(driver)
    }

    fun pressSave(): ProjectAboutTab {
        log.info("Click Save notes")
        clickElement(saveButton)
        return ProjectAboutTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectAboutTab::class.java)
    }
}
