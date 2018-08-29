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
package org.zanata.page.projects

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class CreateProjectPage(driver: WebDriver) : BasePage(driver) {
    private val idField = By.id("project-form:slug:input:slug")
    private val nameField = By.id("project-form:name:input:name")
    private val descriptionField = By.id("project-form:description:input:description")
    private val projectTypeList = By.id("project-types")
    private val createButton = By.id("project-form:create-new")

    fun enterProjectId(projectId: String): CreateProjectPage {
        log.info("Enter project ID {}", projectId)
        enterText(readyElement(idField), projectId)
        return CreateProjectPage(driver)
    }

    fun enterProjectName(projectName: String): CreateProjectPage {
        log.info("Enter project name {}", projectName)
        enterText(readyElement(nameField), projectName)
        return CreateProjectPage(driver)
    }

    fun enterDescription(projectDescription: String): CreateProjectPage {
        log.info("Enter project description {}", projectDescription)
        enterText(readyElement(descriptionField), projectDescription)
        return CreateProjectPage(driver)
    }

    fun selectProjectType(projectType: String): CreateProjectPage {
        log.info("Click project type {}", projectType)
        val projectTypes = readyElement(projectTypeList).findElements(By.tagName("li"))
        for (projectTypeLi in projectTypes) {
            if (projectTypeLi.findElement(By.xpath(".//div/label")).text == projectType) {
                projectTypeLi.findElement(By.xpath(".//div")).click()
                break
            }
        }
        return CreateProjectPage(driver)
    }

    fun pressCreateProject(): ProjectVersionsPage {
        // sometimes the project is created with a partial projectName
        // waitForPageSilence should help ensure projectName has been processed
        waitForPageSilence()
        log.info("Click Create")
        clickAndCheckErrors(readyElement(createButton))
        return ProjectVersionsPage(driver)
    }

    fun pressCreateProjectAndExpectFailure(): CreateProjectPage {
        waitForPageSilence()
        log.info("Click Create")
        clickAndExpectErrors(readyElement(createButton))
        return CreateProjectPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(CreateProjectPage::class.java)
    }
}
