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
package org.zanata.page.projects.projectsettings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.ProjectBasePage;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the Project General Settings tab.
 *
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectGeneralTab extends ProjectBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectGeneralTab.class);
    private By projectIdField = By.id("settings-general-form:slug:input:slug");
    private By projectNameField =
            By.id("settings-general-form:name:input:name");
    private By descriptionField =
            By.id("settings-general-form:description:input:description");
    private By projectTypeList = By.id("project-types");
    private By homepageField =
            By.id("settings-general-form:homePage:input:homePage");
    private By repoField = By.id("settings-general-form:repo:input:repo");
    private By deleteButton = By.id("button-archive-project");
    private By confirmDeleteButton = By.id("deleteButton");
    private By confirmDeleteInput = By.id("confirmDeleteInput");
    private By cancelDeleteButton = By.id("cancelDelete");
    private By lockProjectButton =
            By.id("settings-general-form:button-lock-project");
    private By unlockProjectButton =
            By.id("settings-general-form:button-unlock-project");
    private By updateButton =
            By.id("settings-general-form:button-update-settings");

    public ProjectGeneralTab(WebDriver driver) {
        super(driver);
    }

    /**
     * Get the project ID
     *
     * @return project ID string
     */
    public String getProjectId() {
        return readyElement(projectIdField).getAttribute("value");
    }

    /**
     * Enter a new slug for the project. Removes any existing text.
     *
     * @param projectSlug
     *            new project slug
     * @return new Project General Settings page
     */
    public ProjectGeneralTab enterProjectSlug(String projectSlug) {
        log.info("Enter project slug {}", projectSlug);
        readyElement(projectIdField).clear();
        enterText(readyElement(projectIdField), projectSlug);
        defocus(projectIdField);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Enter a new name for the project. Removes any existing text.
     *
     * @param projectName
     *            new project name
     * @return new Project General Settings page
     */
    public ProjectGeneralTab enterProjectName(final String projectName) {
        log.info("Enter project name {}", projectName);
        readyElement(projectNameField).clear();
        enterText(readyElement(projectNameField), projectName);
        defocus(projectNameField);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Enter a new description for the project. Removes any existing text.
     *
     * @param projectDescription
     *            new project description
     * @return new Project General Settings page
     */
    public ProjectGeneralTab enterDescription(String projectDescription) {
        log.info("Enter project description {}", projectDescription);
        readyElement(descriptionField).clear();
        enterText(readyElement(descriptionField), projectDescription);
        defocus(descriptionField);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Select a new type for the project. Searches by display name.
     *
     * @param projectType
     *            new project type
     * @return new Project General Settings page
     */
    public ProjectGeneralTab selectProjectType(String projectType) {
        log.info("Click Project type {}", projectType);
        assert getProjectTypes().containsKey(projectType);
        WebElement projectTypeButton = getProjectTypes().get(projectType);
        scrollIntoView(projectTypeButton);
        projectTypeButton.click();
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Query the currently selected project type.
     *
     * @return String project type selected
     */
    public String getSelectedProjectType() {
        log.info("Query selected project type");
        for (Map.Entry<String, WebElement> entry : getProjectTypes().entrySet()) {
            if (entry.getValue().findElement(By.tagName("input"))
                    .isSelected()) {
                return entry.getKey();
            }
        }
        return "None";
    }
    // Return a map of project type to div container

    private Map<String, WebElement> getProjectTypes() {
        Map<String, WebElement> types = new HashMap<String, WebElement>();
        for (WebElement projectTypeRow : readyElement(projectTypeList)
                .findElements(By.tagName("li"))) {
            String label =
                    projectTypeRow.findElement(By.tagName("label")).getText();
            String meta = projectTypeRow.findElement(By.className("txt--meta"))
                    .getText();
            types.put(label.substring(0, label.indexOf(meta)).trim(),
                    projectTypeRow.findElement(By.xpath(".//div")));
        }
        return types;
    }

    /**
     * Query for availability of the Archive This Project button. Only
     * Administrators can use this feature.
     *
     * @return button available true/false
     */
    public boolean isDeleteButtonAvailable() {
        log.info("Query is Archive button displayed");
        return getDriver().findElements(deleteButton).size() > 0;
    }

    /**
     * Press the "Delete this project" button
     *
     * @return new Dashboard page
     */
    public ProjectGeneralTab deleteProject() {
        log.info("Click Delete this project");
        clickElement(deleteButton);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Enter exact project name again to confirm the deletion.
     *
     * @param projectName
     *            project name
     * @return this page
     */
    public ProjectGeneralTab
            enterProjectNameToConfirmDelete(String projectName) {
        log.info("Input project name again to confirm");
        readyElement(confirmDeleteInput).clear();
        enterText(readyElement(confirmDeleteInput), projectName);
        waitForPageSilence();
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Confirm project delete
     *
     * @return new Dashboard page
     */
    public ProjectGeneralTab confirmDeleteProject() {
        log.info("Click confirm Delete");
        clickElement(confirmDeleteButton);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Press the "Make this project read only" button
     *
     * @return new Project General Settings page
     */
    public ProjectGeneralTab lockProject() {
        log.info("Click Make this project read only");
        clickElement(lockProjectButton);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Press the "Make this project writable" button
     *
     * @return new Project General Settings page
     */
    public ProjectGeneralTab unlockProject() {
        log.info("Click Make this project writable");
        clickElement(unlockProjectButton);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Enter a new home page url for the project. Removes any existing text.
     *
     * @param homepage
     *            new project home page
     * @return new Project General Settings page
     */
    public ProjectGeneralTab enterHomePage(String homepage) {
        log.info("Enter home page {}", homepage);
        readyElement(homepageField).clear();
        enterText(readyElement(homepageField), homepage);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Enter a new repository url for the project. Removes any existing text.
     *
     * @param repo
     *            new project description
     * @return new Project General Settings page
     */
    public ProjectGeneralTab enterRepository(String repo) {
        log.info("Enter repository {}", repo);
        readyElement(repoField).clear();
        enterText(readyElement(repoField), repo);
        return new ProjectGeneralTab(getDriver());
    }

    /**
     * Press the "Update general settings" button
     *
     * @return new Project General Settings page
     */
    public ProjectGeneralTab updateProject() {
        log.info("Click Update general settings");
        scrollIntoView(readyElement(updateButton));
        clickAndCheckErrors(readyElement(updateButton));
        return new ProjectGeneralTab(getDriver());
    }
}
