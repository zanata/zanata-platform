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
package org.zanata.page.projects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;
import org.zanata.util.Constants;

import java.util.Map;

public class CreateProjectPage extends BasePage {
    @FindBy(id = "projectForm:slugField:slug")
    private WebElement projectIdField;

    @FindBy(id = "projectForm:nameField:name")
    private WebElement projectNameField;

    @FindBy(id = "projectForm:descriptionField:description")
    private WebElement descriptionField;

    @FindBy(id = "projectForm:projectTypeField:selectField")
    private WebElement projectTypeSelect;

    @FindBy(id =
            "projectForm:humanViewableSourceUrlField:humanViewableSourceUrl")
    private WebElement viewSourceURLField;

    @FindBy(id = "projectForm:"+
            "machineReadableSourceUrlField:machineReadableSourceUrl")
    private WebElement downloadSourceURLField;

    @FindBy(id = "cke_projectForm:homeContentField:homeContent:inp")
    private WebElement homeContentTextArea;

    @FindBy(id = "projectForm:statusField:selectField")
    private WebElement statusSelection;

    @FindBy(id = "projectForm:save")
    private WebElement saveButton;

    private static final Map<String, String> projectTypeOptions =
            Constants.projectTypeOptions();

    public CreateProjectPage(final WebDriver driver) {
        super(driver);
    }

    public CreateProjectPage inputProjectId(String projectId) {
        projectIdField.sendKeys(projectId);
        defocus();
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage inputProjectName(final String projectName) {
        getDriver().findElement(By.id("projectForm:nameField:name"))
        .sendKeys(projectName);
        defocus();
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage enterDescription(String projectDescription) {
        descriptionField.sendKeys(projectDescription);
        defocus();
        return new CreateProjectPage(getDriver());
    }

    public CreateProjectPage selectProjectType(String projectType) {
        new Select(projectTypeSelect)
                .selectByVisibleText(projectTypeOptions.get(projectType));
        return this;
    }

    public CreateProjectPage selectStatus(String status) {
        new Select(statusSelection).selectByVisibleText(status);
        return this;
    }

    public CreateProjectPage enterViewSourceURL(String url) {
        viewSourceURLField.sendKeys(url);
        return this;
    }

    public CreateProjectPage enterDownloadSourceURL(String url) {
        downloadSourceURLField.sendKeys(url);
        return this;
    }

    public CreateProjectPage enterHomepageContent(String content) {
        homeContentTextArea.sendKeys(content);
        return this;
    }

    public ProjectPage saveProject() {
        clickAndCheckErrors(saveButton);
        return new ProjectPage(getDriver());
    }
}
