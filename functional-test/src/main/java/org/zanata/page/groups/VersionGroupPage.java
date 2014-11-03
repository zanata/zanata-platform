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
package org.zanata.page.groups;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class VersionGroupPage extends BasePage {

    private By versionsInGroupTableBy = By.id("projects-project_list");

    public VersionGroupPage(final WebDriver driver) {
        super(driver);
    }

    @FindBy(id = "settings-projects-form:newVersionField:newVersionInput")
    private WebElement projectSearchField;

    private final By newVersionListBy = By
            .id("settings-projects-form:newVersionField:newVersionItems");

    public String getGroupName() {
        return getDriver().findElement(By.id("group-info"))
                .findElement(By.tagName("h1")).getText();
    }

    public List<WebElement> searchProject(final String projectName,
            final int expectedResultNum) {
        projectSearchField.sendKeys(projectName);

        return refreshPageUntil(this,
                new Function<WebDriver, List<WebElement>>() {
                    @Override
                    public List<WebElement> apply(WebDriver driver) {
                        // we want to wait until search result comes back. There
                        // is no way we can tell whether search result has come
                        // back and table refreshed.
                        // To avoid the
                        // org.openqa.selenium.StaleElementReferenceException
                        // (http://seleniumhq.org/exceptions/stale_element_reference.html),
                        // we have to set expected result num

                        List<WebElement> listItems =
                                WebElementUtil.getListItems(getDriver(),
                                        newVersionListBy);

                        if (listItems.size() != expectedResultNum) {
                            log.debug("waiting for search result refresh...");
                            return null;
                        }
                        return listItems;
                    }
                });
    }

    public VersionGroupPage addToGroup(int rowIndex) {

        List<WebElement> listItems =
                WebElementUtil.getListItems(getDriver(), newVersionListBy);

        listItems.get(rowIndex).click();

        WebElement addButton =
                getDriver()
                        .findElement(
                                By.id("settings-projects-form:group-add-new-project-button"));
        addButton.click();
        return this;
    }

    /**
     * Get the list of project versions attached to the group
     * @return a list of version group identifiers in the format
     *         "$projectID $version"
     */
    public List<String> getProjectVersionsInGroup() {
        log.info("Query Group project versions");
        List<WebElement> elements = WebElementUtil
                .getListItems(getDriver(), versionsInGroupTableBy);

        List<String> result = new ArrayList<String>();

        for (WebElement element : elements) {
            result.add(element
                    .findElement(By.className("list__item__info"))
                    .getText());
        }
        return result;
    }

    public ProjectVersionsPage clickOnProjectLinkOnRow(int row) {
        List<TableRow> tableRows =
                WebElementUtil
                        .getTableRows(getDriver(), versionsInGroupTableBy);
        WebElement projectLink =
                tableRows.get(row).getCells().get(0)
                        .findElement(By.tagName("a"));
        projectLink.click();
        return new ProjectVersionsPage(getDriver());
    }

    public VersionLanguagesPage clickOnProjectVersionLinkOnRow(int row) {
        List<TableRow> tableRows =
                WebElementUtil
                        .getTableRows(getDriver(), versionsInGroupTableBy);
        WebElement versionLink =
                tableRows.get(row).getCells().get(1)
                        .findElement(By.tagName("a"));
        versionLink.click();
        return new VersionLanguagesPage(getDriver());
    }

    public void clickOnTab(String tabId) {
        WebElement tab = getDriver().findElement(By.id(tabId));
        tab.click();
    }

    public VersionGroupPage clickAddProjectVersionsButton() {
        log.info("Click Add Project Version");
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                WebElement addProjectVersionButton = driver
                        .findElement(By.id("projects-project_form"))
                        .findElement(By.className("button--primary"));
                addProjectVersionButton.click();
                return true;
            }
        });
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage clickLanguagesTab() {
        log.info("Click Languages tab");
        clickWhenTabEnabled(getDriver().findElement(By.id("languages_tab")));
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage clickProjectsTab() {
        log.info("Click Projects tab");
        clickWhenTabEnabled(getDriver().findElement(By.id("projects_tab")));
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage clickMaintainersTab() {
        log.info("Click Maintainers tab");
        clickWhenTabEnabled(getDriver().findElement(By.id("maintainers_tab")));
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage clickSettingsTab() {
        log.info("Click Settings tab");
        clickWhenTabEnabled(getDriver().findElement(By.id("settings_tab")));
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage clickLanguagesSettingsTab() {
        clickSettingsTab();
        getDriver().findElement(By.id("settings-languages_tab")).click();
        return new VersionGroupPage(getDriver());
    }

    public Boolean isLanguagesTabActive() {
        log.info("Query is languages tab displayed");
        final WebElement languagesTab = getDriver().findElement(By.id("languages"));
        waitForAMoment().until( new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver webDriver) {
                return languagesTab.getAttribute("class").contains("is-active");
            }
        } );
        return languagesTab.getAttribute("class").contains("is-active");
    }

    public Boolean isProjectsTabActive() {
        final WebElement languagesTab = getDriver().findElement(By.id("projects"));
        waitForAMoment().until( new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver webDriver) {
                return languagesTab.getAttribute("class").contains("is-active");
            }
        } );
        return languagesTab.getAttribute("class").contains("is-active");
    }

    /**
     * Enter a project version identifier
     * @param projectVersion identifier in format "$projectID $version"
     * @return new VersionGroupPage
     */
    public VersionGroupPage enterProjectVersion(String projectVersion) {
        log.info("Enter project version {}", projectVersion);
        getDriver()
                .findElement(By.id("versionAutocomplete-autocomplete__input"))
                .sendKeys(projectVersion);
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage selectProjectVersion(final String searchEntry) {
        log.info("Click project version {}", searchEntry);
        waitForAMoment().until(
                new Predicate<WebDriver>() {
                    @Override
                    public boolean apply(WebDriver driver) {
                        List<WebElement> items = WebElementUtil
                                .getSearchAutocompleteResults(driver,
                                        "settings-projects-form",
                                        "versionAutocomplete");
                        for (WebElement item : items) {
                            if (item.getText().equals(searchEntry)) {
                                item.click();
                                return true;
                            }
                        }
                        return false;
                    }
                });
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage confirmAddProject() {
        new Actions(getDriver()).sendKeys(Keys.ENTER);
        return new VersionGroupPage(getDriver());
    }

}
