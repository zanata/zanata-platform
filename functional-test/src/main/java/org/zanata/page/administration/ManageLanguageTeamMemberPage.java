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
package org.zanata.page.administration;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.util.Checkbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ManageLanguageTeamMemberPage extends BasePage {

    private By memberPanel = By.id("memberPanel");
    private By joinLanguageTeamButton = By.linkText("Join Language Team");
    private By addTeamMemberButton = By.id("addTeamMemberLink");
    private By addUserSearchInput = By.id("searchForm:searchField");
    private By addUserSearchButton = By.id("searchForm:searchBtn");
    private By personTable = By.id("resultForm:searchResults");
    private By addSelectedButton = By.id("addSelectedBtn");
    private By closeSearchButton = By.className("modal__close");
    private By moreActions = By.id("more-actions");

    public static final int IS_TRANSLATOR_COLUMN = 0;
    public static final int IS_REVIEWER_COLUMN = 1;
    public static final int IS_COORDINATOR_COLUMN = 2;

    public ManageLanguageTeamMemberPage(WebDriver driver) {
        super(driver);
    }

    public ManageLanguageTeamMemberPage clickMoreActions() {
        log.info("Click More Actions dropdown");
        waitForWebElement(moreActions).click();
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    private String getMemberCount() {
        log.info("Query members info");
        return waitForWebElement(By.className("panel__heading"))
                .findElement(By.className("i--users")).getText().trim();
    }

    public List<String> getMemberUsernames() {
        log.info("Query username list");
        if (getMemberCount().equals("0")) {
            log.info("No members yet for this language");
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        for (WebElement listEntry : waitForWebElement(memberPanel)
                .findElements(By.className("list__item--actionable"))) {
            names.add(listEntry.findElement(By.tagName("h3")).getText().trim());
        }
        return names;
    }

    public ManageLanguageTeamMemberPage joinLanguageTeam() {
        log.info("Click Join");
        waitForWebElement(joinLanguageTeamButton).click();
        // we need to wait for this join to finish before returning the page
        waitForAMoment().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return driver.findElements(joinLanguageTeamButton).isEmpty();
            }
        });
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    public ManageLanguageTeamMemberPage clickAddTeamMember() {
        log.info("Click Add Team Member");
        waitForWebElement(addTeamMemberButton).click();
        return this;
    }

    public ManageLanguageTeamMemberPage enterUsername(String username) {
        log.info("Enter username search {}", username);
        waitForWebElement(addUserSearchInput).sendKeys(username);
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    public ManageLanguageTeamMemberPage clickSearch() {
        log.info("Click Search");
        waitForWebElement(addUserSearchButton).click();
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    private WebElement getSearchedForUser(final String username) {
        return waitForAMoment().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                WebElement list = waitForWebElement(personTable)
                        .findElement(By.className("list--slat"));
                List<WebElement> rows = list
                        .findElements(By.className("txt--meta"));
                rows.addAll(list
                        .findElements(By.className("txt--mini")));
                for (WebElement row : rows) {
                    if (getListItemUsername(row).equals(username)) {
                        return row;
                    }
                }
                return null;
            }
        });
    }

    private String getListItemUsername(WebElement listItem) {
        String fullname = listItem.findElements(
                By.className("bx--inline-block"))
                .get(0).getText();
        return fullname.substring(fullname.indexOf('[')+1, fullname.indexOf(']'));
    }

    public ManageLanguageTeamMemberPage clickAddUserRoles(final String username, TeamPermission... permissions) {
        log.info("Click user permissions");
        // if permissions is empty, default add as translator
        Set<TeamPermission> permissionToAdd = Sets.newHashSet(permissions);
        permissionToAdd.add(TeamPermission.Translator);

        for (final TeamPermission permission : permissionToAdd) {
            log.info("Set checked as {}", permission.name());
            waitForAMoment().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver webDriver) {
                    WebElement input = getSearchedForUser(username)
                            .findElement(By.className("list--horizontal"))
                            .findElements(By.tagName("li"))
                            .get(permission.columnIndex)
                            .findElement(By.tagName("input"));
                    Checkbox checkbox = Checkbox.of(input);
                    checkbox.check();
                    return checkbox.checked();
                }
            });
        }
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    public ManageLanguageTeamMemberPage clickAddSelectedButton() {
        log.info("Click Add Selected");
        waitForWebElement(addSelectedButton).click();
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    public ManageLanguageTeamMemberPage clickCloseSearchDialog() {
        log.info("Click Close");
        waitForWebElement(closeSearchButton).click();
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    /*
     * Convenience function for adding a language team member
     */
    public ManageLanguageTeamMemberPage searchPersonAndAddToTeam(
            final String personName, TeamPermission... permissions) {
        // Convenience!
        enterUsername(personName);
        clickSearch();
        clickAddUserRoles(personName, permissions);
        clickAddSelectedButton();
        return confirmAdded(personName);
    }

    private ManageLanguageTeamMemberPage confirmAdded(
            final String personUsername) {
        // we need to wait for the page to refresh
        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return getMemberUsernames().contains(personUsername);
            }
        });
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    public static enum TeamPermission {
        Translator(IS_TRANSLATOR_COLUMN), Reviewer(IS_REVIEWER_COLUMN), Coordinator(IS_COORDINATOR_COLUMN);
        private final int columnIndex;

        TeamPermission(int columnIndex) {
            this.columnIndex = columnIndex;
        }

    }
}
