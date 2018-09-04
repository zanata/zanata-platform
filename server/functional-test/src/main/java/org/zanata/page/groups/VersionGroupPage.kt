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
package org.zanata.page.groups

import java.util.ArrayList

import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.zanata.page.BasePage
import org.zanata.page.projects.ProjectVersionsPage
import org.zanata.page.projectversion.VersionLanguagesPage
import org.zanata.util.WebElementUtil

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class VersionGroupPage(driver: WebDriver) : BasePage(driver) {
    private val versionsInGroupTable = By.id("projects-project_list")
    private val projectForm = By.id("projects-project_form")
    private val projectSearchField = By.id("settings-projects-form:newVersionField:newVersionInput")
    private val projectAddButton = By.id("settings-projects-form:group-add-new-project-button")
    private val newVersionList = By.id("settings-projects-form:newVersionField:newVersionItems")
    private val languageForm = By.id("languages-language_form")
    private val groupNameLabel = By.id("group-info")
    private val groupLanguagesList = By.id("languages-language_list")
    private val languagesTab = By.id("languages_tab")
    private val projectsTab = By.id("projects_tab")
    private val maintainersTab = By.id("maintainers_tab")
    private val settingsTab = By.id("settings_tab")
    private val languagesTabBody = By.id("languages")
    private val projectsTabBody = By.id("projects")
    private val maintainersTabBody = By.id("maintainers")
    private val settingsTabBody = By.id("settings")
    private val settingsLanguagesTab = By.id("settings-languages_tab")

    val groupName: String
        get() = readyElement(groupNameLabel).findElement(By.tagName("h1")).text

    /**
     * Get the list of project versions attached to the group
     *
     * @return a list of version group identifiers in the format "$projectID
     * $version"
     */
    val projectVersionsInGroup: List<String>
        get() {
            log.info("Query Group project versions")
            val elements = WebElementUtil.getListItems(driver, versionsInGroupTable)
            val result = ArrayList<String>()
            for (element in elements) {
                result.add(element.findElement(By.className("list__item__info"))
                        .text)
            }
            return result
        }

    val isLanguagesTabActive: Boolean?
        get() {
            log.info("Query is languages tab displayed")
            val languagesTab = readyElement(By.id("languages"))
            waitForAMoment().withMessage("languages tab is active")
                    .until { languagesTab.getAttribute("class").contains("is-active") }
            return languagesTab.getAttribute("class").contains("is-active")
        }

    @Suppress("unused")
    val isProjectsTabActive: Boolean?
        get() {
            val languagesTab = existingElement(By.id("projects"))
            waitForAMoment().withMessage("projects tab is active")
                    .until { it -> languagesTab.getAttribute("class").contains("is-active") }
            return languagesTab.getAttribute("class").contains("is-active")
        }

    val languagesForGroup: List<String>
        get() {
            log.info("Query Group languages")
            val elements = WebElementUtil.getListItems(driver, groupLanguagesList)
            val result = ArrayList<String>()
            for (element in elements) {
                result.add(element.findElement(By.className("list__item__info"))
                        .text)
            }
            return result
        }

    @Suppress("unused")
    fun searchProject(projectName: String,
                      expectedResultNum: Int): List<WebElement> {
        enterText(readyElement(projectSearchField), projectName)
        return refreshPageUntil<VersionGroupPage, List<WebElement>>(this,
                "Find results of searching for $projectName") {
            // we want to wait until search result comes back. There
            // is no way we can tell whether search result has come
            // back and table refreshed.
            // To avoid the
            // org.openqa.selenium.StaleElementReferenceException
            // (http://seleniumhq.org/exceptions/stale_element_reference.html),
            // we have to set expected result num
            val listItems = WebElementUtil
                    .getListItems(driver, newVersionList)
            if (listItems.size != expectedResultNum) {
                log.debug("waiting for search result refresh...")
                return@refreshPageUntil listItems
            }
            listItems
        }
    }

    @Suppress("unused")
    fun addToGroup(rowIndex: Int): VersionGroupPage {
        WebElementUtil.getListItems(driver, newVersionList)[rowIndex]
                .click()
        clickElement(projectAddButton)
        return VersionGroupPage(driver)
    }

    @Suppress("unused")
    fun clickOnProjectLinkOnRow(row: Int): ProjectVersionsPage {
        val tableRows = WebElementUtil.getTableRows(driver, versionsInGroupTable)
        val projectLink = tableRows[row].cells[0]
                .findElement(By.tagName("a"))
        projectLink.click()
        return ProjectVersionsPage(driver)
    }

    @Suppress("unused")
    fun clickOnProjectVersionLinkOnRow(row: Int): VersionLanguagesPage {
        val tableRows = WebElementUtil.getTableRows(driver, versionsInGroupTable)
        val versionLink = tableRows[row].cells[1]
                .findElement(By.tagName("a"))
        versionLink.click()
        return VersionLanguagesPage(driver)
    }

    fun clickAddProjectVersionsButton(): VersionGroupPage {
        log.info("Click Add Project Version")
        // parent
        readyElement(existingElement(projectForm),
                By.className("button--primary")).click()
        return VersionGroupPage(driver)
    }

    fun clickLanguagesTab(): VersionGroupPage {
        log.info("Click Languages tab")
        existingElement(languagesTabBody)
        clickWhenTabEnabled(readyElement(languagesTab))
        return VersionGroupPage(driver)
    }

    fun clickProjectsTab(): VersionGroupPage {
        log.info("Click Projects tab")
        existingElement(projectsTabBody)
        clickWhenTabEnabled(readyElement(projectsTab))
        return VersionGroupPage(driver)
    }

    fun clickMaintainersTab(): VersionGroupPage {
        log.info("Click Maintainers tab")
        existingElement(maintainersTabBody)
        clickWhenTabEnabled(readyElement(maintainersTab))
        return VersionGroupPage(driver)
    }

    fun clickSettingsTab(): VersionGroupPage {
        log.info("Click Settings tab")
        existingElement(settingsTabBody)
        clickWhenTabEnabled(readyElement(settingsTab))
        return VersionGroupPage(driver)
    }

    @Suppress("unused")
    fun clickLanguagesSettingsTab(): VersionGroupPage {
        clickSettingsTab()
        clickElement(settingsLanguagesTab)
        return VersionGroupPage(driver)
    }

    /**
     * Enter a project version identifier
     *
     * @param projectVersion
     * identifier in format "$projectID $version"
     * @return new VersionGroupPage
     */
    fun enterProjectVersion(projectVersion: String): VersionGroupPage {
        log.info("Enter project version {}", projectVersion)
        enterText(readyElement(By.id("versionAutocomplete-autocomplete__input")),
                projectVersion)
        return VersionGroupPage(driver)
    }

    fun selectProjectVersion(searchEntry: String): VersionGroupPage {
        log.info("Click project version {}", searchEntry)
        waitForAMoment().withMessage("project version is clicked")
                .until { driver ->
                    val items = WebElementUtil.getSearchAutocompleteResults(driver,
                            "settings-projects-form", "versionAutocomplete")
                    for (item in items) {
                        if (item.text == searchEntry) {
                            item.click()
                            return@until true
                        }
                    }
                    false
                }
        return VersionGroupPage(driver)
    }

    @Suppress("unused")
    fun confirmAddProject(): VersionGroupPage {
        Actions(driver).sendKeys(Keys.ENTER)
        return VersionGroupPage(driver)
    }

    fun clickAddLanguagesButton(): VersionGroupPage {
        log.info("Click Add Languages Button")
        // parent
        readyElement(existingElement(languageForm), By.id("addLanguagesButton"))
                .click()
        return VersionGroupPage(driver)
    }

    fun activateLanguageList(): VersionGroupPage {
        log.info("Activate language list")
        readyElement(By.id("languageAutocomplete-autocomplete__input"))
                .sendKeys("")
        return VersionGroupPage(driver)
    }

    fun selectLanguage(searchEntry: String): VersionGroupPage {
        log.info("Click language {}", searchEntry)
        waitForAMoment().withMessage("language is clicked")
                .until { driver ->
                    val items = WebElementUtil.getSearchAutocompleteResults(driver,
                            "settings-languages-form", "languageAutocomplete")
                    for (item in items) {
                        if (item.text == searchEntry) {
                            item.click()
                            return@until true
                        }
                    }
                    false
                }
        return VersionGroupPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(VersionGroupPage::class.java)
    }
}
