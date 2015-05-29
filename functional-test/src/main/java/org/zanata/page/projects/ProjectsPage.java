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

import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ProjectsPage extends BasePage {

    public static final int PROJECT_NAME_COLUMN = 0;

    private By createProjectButton = By.id("createProjectLink");
    private By mainContentDiv = By.id("main_body_content");
    private By projectTable = By.id("main_content:form:projectList");
    private By activeCheckBox = By.xpath("//*[@data-original-title='Filter active projects']");
    private By readOnlyCheckBox = By.xpath("//*[@data-original-title='Filter read-only projects']");

    public ProjectsPage(final WebDriver driver) {
        super(driver);
    }

    public CreateProjectPage clickOnCreateProjectLink() {
        log.info("Click Create Project");
        readyElement(createProjectButton).click();
        return new CreateProjectPage(getDriver());
    }

    public ProjectVersionsPage goToProject(final String projectName) {
        log.info("Click Project {}", projectName);
        // TODO this can't handle project on different page
        return refreshPageUntil(this, new Function<WebDriver, ProjectVersionsPage>() {
            @Override
            public ProjectVersionsPage apply(WebDriver input) {
                WebElement table = input.findElement(projectTable);
                log.info("current projects: {}", WebElementUtil
                        .getColumnContents(input, projectTable,
                                PROJECT_NAME_COLUMN));
                WebElement link = table.findElement(By.linkText(projectName));
                link.click();
                return new ProjectVersionsPage(getDriver());
            }
        }, "Find and go to project " + projectName);
    }

    public List<String> getProjectNamesOnCurrentPage() {
        log.info("Query Projects list");
        if (readyElement(mainContentDiv)
                .getText().contains("No project exists")) {
            return Collections.emptyList();
        }

        return WebElementUtil.getColumnContents(getDriver(), projectTable,
                PROJECT_NAME_COLUMN);
    }

    /**
     * Wait for a project name to be shown in the list.
     * The list may take several seconds to reload.
     * @param projectName name of the desired project
     */
    public ProjectsPage expectProjectVisible(final String projectName) {
        log.info("Wait for project {} visible", projectName);
        waitForPageSilence();
        assertThat(getProjectNamesOnCurrentPage()).contains(projectName);
        return new ProjectsPage(getDriver());
    }

    /**
     * Wait for a project name to be hidden in the list.
     * The list may take several seconds to reload.
     * @param projectName name of the desired project
     */
    public ProjectsPage expectProjectNotVisible(final String projectName) {
        log.info("Wait for project {} not visible", projectName);
        waitForPageSilence();
        assertThat(getProjectNamesOnCurrentPage()).doesNotContain(
                projectName);
        return new ProjectsPage(getDriver());
    }

    public ProjectsPage setActiveFilterEnabled(boolean enabled) {
        log.info("Click to set Active filter enabled to {}", enabled);
        WebElement activeCheckbox = readyElement(activeCheckBox);
        if (activeCheckbox.isSelected() != enabled) {
            activeCheckbox.click();
        }
        return new ProjectsPage(getDriver());
    }

    public ProjectsPage setReadOnlyFilterEnabled(final boolean enabled) {
        log.info("Click to set Read-only filter enabled to {}", enabled);
        WebElement readOnlyCheckbox = readyElement(readOnlyCheckBox);
        if (readOnlyCheckbox.isSelected() != enabled) {
            readOnlyCheckbox.click();
        }
        return new ProjectsPage(getDriver());
    }

}
