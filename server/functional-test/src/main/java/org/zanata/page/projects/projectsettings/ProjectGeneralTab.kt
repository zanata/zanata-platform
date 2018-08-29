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
import org.openqa.selenium.WebElement
import org.zanata.page.projects.ProjectBasePage
import java.util.HashMap

/**
 * This class represents the Project General Settings tab.
 *
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ProjectGeneralTab(driver: WebDriver) : ProjectBasePage(driver) {
    private val projectIdField = By.id("settings-general-form:slug:input:slug")
    private val projectNameField = By.id("settings-general-form:name:input:name")
    private val descriptionField = By.id("settings-general-form:description:input:description")
    private val projectTypeList = By.id("project-types")
    private val homepageField = By.id("settings-general-form:homePage:input:homePage")
    private val repoField = By.id("settings-general-form:repo:input:repo")
    private val deleteButton = By.id("button-archive-project")
    private val confirmDeleteButton = By.id("deleteButton")
    private val confirmDeleteInput = By.id("confirmDeleteInput")
    private val lockProjectButton = By.id("settings-general-form:button-lock-project")
    private val unlockProjectButton = By.id("settings-general-form:button-unlock-project")
    private val updateButton = By.id("settings-general-form:button-update-settings")

    /**
     * Get the project ID
     *
     * @return project ID string
     */
    val projectId: String
        get() = readyElement(projectIdField).getAttribute("value")

    /**
     * Query the currently selected project type.
     *
     * @return String project type selected
     */
    val selectedProjectType: String
        get() {
            log.info("Query selected project type")
            for ((key, value) in projectTypes) {
                if (value.findElement(By.tagName("input"))
                                .isSelected) {
                    return key
                }
            }
            return "None"
        }
    // Return a map of project type to div container

    private val projectTypes: Map<String, WebElement>
        get() {
            val types = HashMap<String, WebElement>()
            for (projectTypeRow in readyElement(projectTypeList)
                    .findElements(By.tagName("li"))) {
                val label = projectTypeRow.findElement(By.tagName("label")).text
                val meta = projectTypeRow.findElement(By.className("txt--meta"))
                        .text
                types[label.substring(0, label.indexOf(meta)).trim { it <= ' ' }] = projectTypeRow.findElement(By.xpath(".//div"))
            }
            return types
        }

    /**
     * Query for availability of the Archive This Project button. Only
     * Administrators can use this feature.
     *
     * @return button available true/false
     */
    @Suppress("unused")
    val isDeleteButtonAvailable: Boolean
        get() {
            log.info("Query is Archive button displayed")
            return driver.findElements(deleteButton).size > 0
        }

    /**
     * Enter a new slug for the project. Removes any existing text.
     *
     * @param projectSlug
     * new project slug
     * @return new Project General Settings page
     */
    fun enterProjectSlug(projectSlug: String): ProjectGeneralTab {
        log.info("Enter project slug {}", projectSlug)
        readyElement(projectIdField).clear()
        enterText(readyElement(projectIdField), projectSlug)
        defocus(projectIdField)
        return ProjectGeneralTab(driver)
    }

    /**
     * Enter a new name for the project. Removes any existing text.
     *
     * @param projectName
     * new project name
     * @return new Project General Settings page
     */
    fun enterProjectName(projectName: String): ProjectGeneralTab {
        log.info("Enter project name {}", projectName)
        readyElement(projectNameField).clear()
        enterText(readyElement(projectNameField), projectName)
        defocus(projectNameField)
        return ProjectGeneralTab(driver)
    }

    /**
     * Enter a new description for the project. Removes any existing text.
     *
     * @param projectDescription
     * new project description
     * @return new Project General Settings page
     */
    fun enterDescription(projectDescription: String): ProjectGeneralTab {
        log.info("Enter project description {}", projectDescription)
        readyElement(descriptionField).clear()
        enterText(readyElement(descriptionField), projectDescription)
        defocus(descriptionField)
        return ProjectGeneralTab(driver)
    }

    /**
     * Select a new type for the project. Searches by display name.
     *
     * @param projectType
     * new project type
     * @return new Project General Settings page
     */
    fun selectProjectType(projectType: String): ProjectGeneralTab {
        log.info("Click Project type {}", projectType)
        assert(projectTypes.containsKey(projectType))
        val projectTypeButton = projectTypes[projectType]
        scrollIntoView(projectTypeButton!!)
        projectTypeButton.click()
        return ProjectGeneralTab(driver)
    }

    /**
     * Press the "Delete this project" button
     *
     * @return new Dashboard page
     */
    fun deleteProject(): ProjectGeneralTab {
        log.info("Click Delete this project")
        clickElement(deleteButton)
        return ProjectGeneralTab(driver)
    }

    /**
     * Enter exact project name again to confirm the deletion.
     *
     * @param projectName
     * project name
     * @return this page
     */
    fun enterProjectNameToConfirmDelete(projectName: String): ProjectGeneralTab {
        log.info("Input project name again to confirm")
        readyElement(confirmDeleteInput).clear()
        enterText(readyElement(confirmDeleteInput), projectName)
        waitForPageSilence()
        return ProjectGeneralTab(driver)
    }

    /**
     * Confirm project delete
     *
     * @return new Dashboard page
     */
    fun confirmDeleteProject(): ProjectGeneralTab {
        log.info("Click confirm Delete")
        clickElement(confirmDeleteButton)
        return ProjectGeneralTab(driver)
    }

    /**
     * Press the "Make this project read only" button
     *
     * @return new Project General Settings page
     */
    fun lockProject(): ProjectGeneralTab {
        log.info("Click Make this project read only")
        clickElement(lockProjectButton)
        return ProjectGeneralTab(driver)
    }

    /**
     * Press the "Make this project writable" button
     *
     * @return new Project General Settings page
     */
    fun unlockProject(): ProjectGeneralTab {
        log.info("Click Make this project writable")
        clickElement(unlockProjectButton)
        return ProjectGeneralTab(driver)
    }

    /**
     * Enter a new home page url for the project. Removes any existing text.
     *
     * @param homepage
     * new project home page
     * @return new Project General Settings page
     */
    fun enterHomePage(homepage: String): ProjectGeneralTab {
        log.info("Enter home page {}", homepage)
        readyElement(homepageField).clear()
        enterText(readyElement(homepageField), homepage)
        return ProjectGeneralTab(driver)
    }

    /**
     * Enter a new repository url for the project. Removes any existing text.
     *
     * @param repo
     * new project description
     * @return new Project General Settings page
     */
    fun enterRepository(repo: String): ProjectGeneralTab {
        log.info("Enter repository {}", repo)
        readyElement(repoField).clear()
        enterText(readyElement(repoField), repo)
        return ProjectGeneralTab(driver)
    }

    /**
     * Press the "Update general settings" button
     *
     * @return new Project General Settings page
     */
    fun updateProject(): ProjectGeneralTab {
        log.info("Click Update general settings")
        scrollIntoView(readyElement(updateButton))
        clickAndCheckErrors(readyElement(updateButton))
        return ProjectGeneralTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectGeneralTab::class.java)
    }
}
