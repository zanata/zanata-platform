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
package org.zanata.page.projects;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;

public class CreateProjectPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CreateProjectPage.class);
    private By idField = By.id("project-form:slug:input:slug");
    private By nameField = By.id("project-form:name:input:name");
    private By descriptionField =
            By.id("project-form:description:input:description");
    private By projectTypeList = By.id("project-types");
    private By createButton = By.id("project-form:create-new");

    public CreateProjectPage(final WebDriver driver) {
        super(driver);
    }

    public CreateProjectPage enterProjectId(String projectId) {
        log.info("Enter project ID {}", projectId);
        enterText(readyElement(idField), projectId);
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage enterProjectName(final String projectName) {
        log.info("Enter project name {}", projectName);
        enterText(readyElement(nameField), projectName);
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage enterDescription(String projectDescription) {
        log.info("Enter project description {}", projectDescription);
        enterText(readyElement(descriptionField), projectDescription);
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage selectProjectType(String projectType) {
        log.info("Click project type {}", projectType);
        List<WebElement> projectTypes =
                readyElement(projectTypeList).findElements(By.tagName("li"));
        for (WebElement projectTypeLi : projectTypes) {
            if (projectTypeLi.findElement(By.xpath(".//div/label")).getText()
                    .equals(projectType)) {
                projectTypeLi.findElement(By.xpath(".//div")).click();
                break;
            }
        }
        return new CreateProjectPage(getDriver());
    }

    public ProjectVersionsPage pressCreateProject() {
        log.info("Click Create");
        clickAndCheckErrors(readyElement(createButton));
        return new ProjectVersionsPage(getDriver());
    }
}
