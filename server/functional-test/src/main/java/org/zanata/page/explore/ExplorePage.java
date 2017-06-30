/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.explore;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.page.account.ProfilePage;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.page.languages.LanguagePage;
import org.zanata.page.projects.ProjectVersionsPage;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ExplorePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ExplorePage.class);
    private static final By searchInput = By.id("explore_search");
    private static final By projectResult = By.id("explore_Project_result");
    private static final By personResult = By.id("explore_Person_result");
    private static final By groupResult = By.id("explore_Group_result");
    private static final By languageTeamResult =
            By.id("explore_LanguageTeam_result");

    public ExplorePage(WebDriver driver) {
        super(driver);
    }

    public ExplorePage clearSearch() {
        log.info("Clear search field");
        readyElement(searchInput).clear();
        return new ExplorePage(getDriver());
    }

    public boolean isCancelButtonEnabled() {
        log.info("Query cancel button is enabled");
        return readyElement(searchInput).isEnabled();
    }

    public boolean isSearchFieldCleared() {
        log.info("Query is search field clear");
        return readyElement(searchInput).getText().equals("");
    }

    public ExplorePage enterSearch(String searchText) {
        log.info("Enter Explore search {}", searchText);
        existingElement(searchInput).sendKeys(searchText);
        waitForAMoment().withMessage("Waiting for search complete")
                .until(it -> !isProjectSearchLoading()
                        && !isGroupSearchLoading()
                        && !isLanguageTeamSearchLoading()
                        && !isPersonSearchLoading());
        return new ExplorePage(getDriver());
    }

    public ExplorePage expectProjectListContains(final String expected) {
        String msg = "Project search list contains " + expected;
        log.info("Expect {}", msg);
        waitForAMoment().withMessage("Waiting for search contains")
                .until(it -> getProjectSearchResults()
                        .contains(expected));
        assertThat(getProjectSearchResults()).as(msg).contains(expected);
        return new ExplorePage(getDriver());
    }

    public ExplorePage expectGroupListContains(final String expected) {
        String msg = "Group search list contains " + expected;
        log.info("Expect {}", msg);
        waitForAMoment().withMessage("Waiting for search contains")
                .until(webDriver -> getGroupSearchResults()
                        .contains(expected));
        assertThat(getGroupSearchResults()).as(msg).contains(expected);
        return new ExplorePage(getDriver());
    }

    public ExplorePage expectPersonListContains(final String expected) {
        waitForPageSilence();
        String msg = "Person search list contains " + expected;
        log.info("Expect {}", msg);
        waitForAMoment().withMessage("Waiting for search contains")
                .until(it -> getUserSearchResults()
                        .contains(expected));
        assertThat(getUserSearchResults()).as(msg).contains(expected);
        return new ExplorePage(getDriver());
    }

    public ExplorePage expectLanguageTeamListContains(final String expected) {
        String msg = "Language Team search list contains " + expected;
        log.info("Expect {}", msg);
        waitForAMoment().withMessage("Waiting for search contains")
                .until(webDriver -> getLanguageSearchResults()
                        .contains(expected));
        assertThat(getLanguageSearchResults()).as(msg).contains(expected);
        return new ExplorePage(getDriver());
    }

    public List<String> getProjectSearchResults() {
        log.info("Query Project search results list");
        return getResultText(projectResult);
    }

    public List<String> getGroupSearchResults() {
        log.info("Query Group search results list");
        return getResultText(groupResult);
    }

    public List<String> getUserSearchResults() {
        log.info("Query User search results list");
        return getResultText(personResult);
    }

    public List<String> getLanguageSearchResults() {
        log.info("Query Language search results list");
        return getResultText(languageTeamResult);
    }

    public boolean isProjectSearchLoading() {
        return isSearchLoading(projectResult);
    }

    public boolean isGroupSearchLoading() {
        return isSearchLoading(groupResult);
    }

    public boolean isPersonSearchLoading() {
        return isSearchLoading(personResult);
    }

    public boolean isLanguageTeamSearchLoading() {
        return isSearchLoading(languageTeamResult);
    }

    private boolean isSearchLoading(By by) {
        return !existingElement(by).findElements(By.name("loader")).isEmpty();
    }

    private List<String> getResultText(By by) {
        List<WebElement> entries =
                existingElement(by).findElements(By.name("entry"));
        List<String> list = Lists.newArrayList();
        for (WebElement element : entries) {
            WebElement aTag = element.findElement(By.tagName("a"));
            if (aTag != null) {
                list.add(aTag.getText());
            }
        }
        return list;
    }

    public ProfilePage clickUserSearchEntry(final String searchEntry) {
        log.info("Click user search result {}", searchEntry);
        List<WebElement> users =
                existingElement(personResult).findElements(By.name("entry"));
        for (WebElement element : users) {
            WebElement aTag = element.findElement(By.tagName("a"));
            if (aTag != null && aTag.getText().equals(searchEntry)) {
                clickElement(aTag);
                break;
            }
        }
        return new ProfilePage(getDriver());
    }

    public ProjectVersionsPage clickProjectEntry(String searchEntry) {
        log.info("Click Projects search result {}", searchEntry);
        List<WebElement> projects =
                existingElement(projectResult).findElements(By.name("entry"));
        for (WebElement element : projects) {
            WebElement aTag = element.findElement(By.tagName("a"));
            if (aTag != null && aTag.getText().equals(searchEntry)) {
                clickElement(aTag);
                break;
            }
        }
        return new ProjectVersionsPage(getDriver());
    }

    public VersionGroupPage clickGroupSearchEntry(final String searchEntry) {
        log.info("Click group search result {}", searchEntry);
        List<WebElement> groups =
                existingElement(groupResult).findElements(By.name("entry"));
        for (WebElement element : groups) {
            WebElement aTag = element.findElement(By.tagName("a"));
            if (aTag != null && aTag.getText().equals(searchEntry)) {
                clickElement(aTag);
                break;
            }
        }
        return new VersionGroupPage(getDriver());
    }

    public LanguagePage clickLangSearchEntry(final String searchEntry) {
        log.info("Click language search result {}", searchEntry);
        List<WebElement> langs =
                existingElement(groupResult).findElements(By.name("entry"));
        for (WebElement element : langs) {
            WebElement aTag = element.findElement(By.tagName("a"));
            if (aTag != null && aTag.getText().equals(searchEntry)) {
                clickElement(aTag);
                break;
            }
        }
        return new LanguagePage(getDriver());
    }

    public ProjectVersionsPage searchAndGotoProjectByName(String projectName) {
        log.info("go to project by name with name: {}", projectName);
        if (getProjectSearchResults().contains(projectName)) {
            return clickProjectEntry(projectName);
        } else {
            return enterSearch(projectName).clickProjectEntry(projectName);
        }
    }

    public VersionGroupPage searchAndGotoGroupByName(String groupName) {
        log.info("Go to project by name with name: {}", groupName);
        if (getGroupSearchResults().contains(groupName)) {
            return clickGroupSearchEntry(groupName);
        } else {
            return enterSearch(groupName).clickGroupSearchEntry(groupName);
        }
    }

}
