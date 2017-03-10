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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.page.account.ProfilePage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.util.WebElementUtil;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

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

    public ExplorePage enterSearch(String searchText) {
        log.info("Enter Explore search {}", searchText);
        existingElement(searchInput).sendKeys(searchText);
        waitForAMoment().withMessage("Waiting for search complete")
                .until((Predicate<WebDriver>) webDriver -> !isProjectSearchLoading()
                        && !isGroupSearchLoading()
                        && !isLanguageTeamSearchLoading()
                        && !isPersonSearchLoading());
        return new ExplorePage(getDriver());
    }

    public ExplorePage expectProjectListContains(final String expected) {
        String msg = "Project search list contains " + expected;
        waitForAMoment().withMessage("Waiting for search contains")
                .until((Predicate<WebDriver>) webDriver -> getProjectSearchResults()
                        .contains(expected));
        assertThat(getProjectSearchResults()).as(msg).contains(expected);
        return new ExplorePage(getDriver());
    }

    public ExplorePage expectPersonListContains(final String expected) {
        waitForPageSilence();
        String msg = "Person search list contains " + expected;
        waitForAMoment().withMessage("Waiting for search contains")
                .until((Predicate<WebDriver>) webDriver -> getUserSearchResults()
                        .contains(expected));
        assertThat(getUserSearchResults()).as(msg).contains(expected);
        return new ExplorePage(getDriver());
    }

    public List<String> getProjectSearchResults() {
        log.info("Query Project search results list");
        return getResultText(projectResult);
    }

    public List<String> getUserSearchResults() {
        log.info("Query User search results list");
        return getResultText(personResult);
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

    public ProjectVersionsPage searchAndGotoProjectByName(String projectName) {
        log.info("go to project by name with name: {}", projectName);
        if (getProjectSearchResults().contains(projectName)) {
            return clickProjectEntry(projectName);
        } else {
            return enterSearch(projectName).clickProjectEntry(projectName);
        }
    }
}
