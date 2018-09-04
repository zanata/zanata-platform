/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.projects

import java.util.ArrayList
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.projects.projectsettings.ProjectAboutTab
import org.zanata.page.projects.projectsettings.ProjectGeneralTab
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab
import org.zanata.page.projects.projectsettings.ProjectPermissionsTab
import org.zanata.page.projects.projectsettings.ProjectTranslationTab
import org.zanata.page.projects.projectsettings.ProjectWebHooksTab

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
open class ProjectBasePage(driver: WebDriver) : BasePage(driver) {
    private val versionsTab = By.id("versions_tab")
    private val peopleTab = By.id("people_tab")
    private val aboutTab = By.id("about_tab")
    private val settingsTab = By.id("settings_tab")
    private val versionsTabBody = By.id("versions")
    private val peopleTabBody = By.id("people")
    private val aboutTabBody = By.id("about")
    private val settingsTabBody = By.id("settings")
    private val settingsGeneralTab = By.id("settings-general_tab")
    private val settingsPermissionTab = By.id("settings-permissions_tab")
    private val settingsTranslationTab = By.id("settings-translation_tab")
    private val settingsLanguagesTab = By.id("settings-languages_tab")
    private val settingsAboutTab = By.id("settings-about_tab")
    private val projectInfo = By.id("project-info")
    private val settingsWebHooksTab = By.id("settings-webhooks_tab")

    val projectName: String
        get() {
            log.info("Query Project name")
            return readyElement(projectInfo).findElement(By.tagName("h1"))
                    .text
        }

    val contentAreaParagraphs: List<String>
        get() {
            log.info("Query Project info")
            val paragraphTexts = ArrayList<String>()
            val paragraphs = readyElement(projectInfo).findElements(By.tagName("p"))
            for (element in paragraphs) {
                paragraphTexts.add(element.text)
            }
            return paragraphTexts
        }

    val homepage: String
        get() {
            log.info("Query Project homepage")
            for (element in readyElement(projectInfo)
                    .findElements(By.tagName("li"))) {
                if (element.findElement(By.className("list__title")).text
                                .trim { it <= ' ' } == "Home Page:") {
                    return element.findElement(By.tagName("a")).text
                }
            }
            return ""
        }

    val gitUrl: String
        get() {
            log.info("Query Project repo")
            for (element in readyElement(projectInfo)
                    .findElements(By.tagName("li"))) {
                if (element.findElement(By.className("list__title")).text
                                .trim { it <= ' ' } == "Repository:") {
                    return element.findElement(By.tagName("input"))
                            .getAttribute("value")
                }
            }
            return ""
        }

    fun gotoVersionsTab(): ProjectVersionsPage {
        log.info("Click Versions tab")
        existingElement(versionsTabBody)
        clickWhenTabEnabled(readyElement(versionsTab))
        readyElement(By.id("versions"))
        return ProjectVersionsPage(driver)
    }

    fun gotoPeopleTab(): ProjectPeoplePage {
        log.info("Click People tab")
        existingElement(peopleTabBody)
        clickWhenTabEnabled(readyElement(peopleTab))
        readyElement(peopleTabBody)
        return ProjectPeoplePage(driver)
    }

    fun gotoAboutTab(): ProjectAboutPage {
        log.info("Click About tab")
        existingElement(aboutTabBody)
        clickWhenTabEnabled(readyElement(aboutTab))
        readyElement(By.id("about"))
        return ProjectAboutPage(driver)
    }

    fun settingsTabIsDisplayed(): Boolean {
        log.info("Query Settings tab is displayed")
        return existingElement(settingsTab).isDisplayed
    }

    fun gotoSettingsTab(): ProjectBasePage {
        log.info("Click Settings tab")
        existingElement(settingsTabBody)
        clickWhenTabEnabled(readyElement(settingsTab))
        readyElement(settingsTab)
        return ProjectBasePage(driver)
    }

    fun gotoSettingsGeneral(): ProjectGeneralTab {
        log.info("Click General settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsGeneralTab))
        readyElement(By.id("settings-general"))
        return ProjectGeneralTab(driver)
    }

    fun gotoSettingsPermissionsTab(): ProjectPermissionsTab {
        log.info("Click Permissions settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsPermissionTab))
        readyElement(By.id("settings-permissions"))
        return ProjectPermissionsTab(driver)
    }

    fun gotoSettingsTranslationTab(): ProjectTranslationTab {
        log.info("Click Translation settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsTranslationTab))
        readyElement(By.id("settings-translation"))
        return ProjectTranslationTab(driver)
    }

    fun gotoSettingsLanguagesTab(): ProjectLanguagesTab {
        log.info("Click Languages settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsLanguagesTab))
        readyElement(By.id("settings-languages"))
        return ProjectLanguagesTab(driver)
    }

    fun gotoSettingsWebHooksTab(): ProjectWebHooksTab {
        log.info("Click WebHooks settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsWebHooksTab))
        readyElement(By.id("settings-webhooks"))
        return ProjectWebHooksTab(driver)
    }

    fun gotoSettingsAboutTab(): ProjectAboutTab {
        log.info("Click About settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsAboutTab))
        readyElement(By.id("settings-about"))
        return ProjectAboutTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectBasePage::class.java)
    }
}
