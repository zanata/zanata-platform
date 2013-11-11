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
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ProjectsPage extends BasePage {
    public static final int PROJECT_NAME_COLUMN = 0;
    @FindBy(id = "main_body_content")
    private WebElement mainContentDiv;

    private By projectTableBy = By.id("main_content:form:projectList");

    public ProjectsPage(final WebDriver driver) {
        super(driver);
    }

    public CreateProjectPage clickOnCreateProjectLink() {
        WebElement createProjectActionLink =
                waitForTenSec().until(new Function<WebDriver, WebElement>() {
                    @Override
                    public WebElement apply(WebDriver driver) {
                        return driver.findElement(By.id("createProjectLink"));
                    }
                });
        createProjectActionLink.click();
        return new CreateProjectPage(getDriver());
    }

    public ProjectPage goToProject(final String projectName) {
        // TODO this can't handle project on different page
        return refreshPageUntil(this, new Function<WebDriver, ProjectPage>() {
            @Override
            public ProjectPage apply(WebDriver input) {
                WebElement table = input.findElement(projectTableBy);
                log.info("current projects: {}", WebElementUtil
                        .getColumnContents(input, projectTableBy,
                                PROJECT_NAME_COLUMN));
                WebElement link = table.findElement(By.linkText(projectName));
                link.click();
                return new ProjectPage(input);
            }
        });
    }

    public List<String> getProjectNamesOnCurrentPage() {
        if (mainContentDiv.getText().contains("No project exists")) {
            return Collections.emptyList();
        }

        return WebElementUtil.getColumnContents(getDriver(), projectTableBy,
                PROJECT_NAME_COLUMN);
    }

    /**
     * Wait for a project name to be shown or hidden in the list.
     * The list may take several seconds to reload.
     * @param projectName name of the desired project
     * @param visible is or is not visible
     */
    public void waitForProjectVisibility(final String projectName,
                                            final boolean visible) {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getProjectNamesOnCurrentPage()
                        .contains(projectName) == visible;
            }
        });
    }

    public ProjectsPage setActiveFilterEnabled(boolean enabled) {
        WebElement activeCheckbox = getDriver()
                .findElement(By.xpath("//*[@title='Filter active projects']"));
        if (activeCheckbox.isSelected() != enabled) {
            activeCheckbox.click();
        }
        return new ProjectsPage(getDriver());
    }

    public ProjectsPage setReadOnlyFilterEnabled(final boolean enabled) {
        WebElement readOnlyCheckbox = getDriver().findElement(
                By.xpath("//*[@title='Filter read-only projects']"));
        if (readOnlyCheckbox.isSelected() != enabled) {
            readOnlyCheckbox.click();
        }
        return new ProjectsPage(getDriver());
    }

    public ProjectsPage setObsoleteFilterEnabled(boolean enabled) {
        WebElement readOnlyCheckbox = getDriver().findElement(
                By.xpath("//*[@title='Filter obsolete projects']"));
        if (readOnlyCheckbox.isSelected() != enabled) {
            readOnlyCheckbox.click();
        }
        return new ProjectsPage(getDriver());
    }
}
