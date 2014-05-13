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
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;

public class CreateProjectPage extends BasePage {

    @FindBy(id = "project-form:descriptionField:description")
    private WebElement descriptionField;

    @FindBy(id = "project-types")
    private WebElement projectTypeList;

    @FindBy(id = "project-form:create-new")
    private WebElement createButton;

    public CreateProjectPage(final WebDriver driver) {
        super(driver);
    }

    public CreateProjectPage enterProjectId(String projectId) {
        getDriver().findElement(By.id("project-form:slugField:slug"))
                .sendKeys(projectId);
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage enterProjectName(final String projectName) {
        getDriver().findElement(By.id("project-form:nameField:name"))
                .sendKeys(projectName);
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage enterDescription(String projectDescription) {
        descriptionField.sendKeys(projectDescription);
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage selectProjectType(String projectType) {
        List<WebElement> projectTypes =
                projectTypeList.findElements(By.tagName("li"));

        for (WebElement projectTypeLi : projectTypes) {
            if (projectTypeLi.findElement(By.xpath(".//div/label")).getText()
                    .equals(projectType)) {
                projectTypeLi.findElement(By.xpath(".//div")).click();
                break;
            }
        }
        return this;
    }

    public ProjectVersionsPage pressCreateProject() {
        clickAndCheckErrors(createButton);
        return new ProjectVersionsPage(getDriver());
    }
}
