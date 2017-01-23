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
package org.zanata.page.languages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.util.Checkbox;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class LanguagePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LanguagePage.class);
    private By contactCoordinatorsButton = By.id("contact-coordinator");
    private By saveButton = By.id("save-button");
    private By moreActions = By.id("more-action");
    private By enableByDefault = By.id("enable-by-default");
    private By membersTab = By.id("members_tab");
    private By settingsTab = By.id("settings_tab");
    private By joinLanguageTeamButton = By.linkText("Join Language Team");
    private By addTeamMemberButton = By.id("add-team-member-button");
    private By addUserSearchInput = By.id("searchForm:searchField");
    private By addUserSearchButton = By.id("searchForm:searchBtn");
    private By personTable = By.id("resultForm:searchResults");
    private By addSelectedButton = By.id("addSelectedBtn");
    public static final int IS_TRANSLATOR_COLUMN = 0;
    public static final int IS_REVIEWER_COLUMN = 1;
    public static final int IS_COORDINATOR_COLUMN = 2;

    public LanguagePage(WebDriver driver) {
        super(driver);
    }

    public LanguagePage clickMoreActions() {
        log.info("Click More Actions");
        clickElement(moreActions);
        return new LanguagePage(getDriver());
    }

    public ContactTeamPage clickContactCoordinatorsButton() {
        log.info("Click Contact Coordinators button");
        clickElement(contactCoordinatorsButton);
        return new ContactTeamPage(getDriver());
    }

    public LanguagePage gotoSettingsTab() {
        clickElement(settingsTab);
        return new LanguagePage(getDriver());
    }

    public LanguagePage gotoMembersTab() {
        clickElement(membersTab);
        return new LanguagePage(getDriver());
    }

    public LanguagePage enableLanguageByDefault(boolean enable) {
        Checkbox checkbox = Checkbox.of(readyElement(enableByDefault));
        if (enable) {
            checkbox.check();
        } else {
            checkbox.uncheck();
        }
        return new LanguagePage(getDriver());
    }

    public LanguagePage saveSettings() {
        clickElement(saveButton);
        return new LanguagePage(getDriver());
    }

    public List<String> getMemberUsernames() {
        log.info("Query username list");
        if (getMemberCount().equals("0")) {
            log.info("No members yet for this language");
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        WebElement form = existingElement(By.id("members-form"));
        for (WebElement listEntry : form
                .findElements(By.className("list__item--actionable"))) {
            names.add(listEntry.findElement(By.className("list__item__info"))
                    .getText().trim());
        }
        log.info("Found {}", names);
        return names;
    }

    private String getMemberCount() {
        log.info("Query members info");
        return readyElement(By.id("members-size")).getText().trim();
    }

    public LanguagePage joinLanguageTeam() {
        log.info("Click Join");
        clickElement(joinLanguageTeamButton);
        // we need to wait for this join to finish before returning the page
        waitForAMoment().until(new Function<WebDriver, Boolean>() {

            @Override
            public Boolean apply(WebDriver driver) {
                return driver.findElements(joinLanguageTeamButton).isEmpty();
            }
        });
        return new LanguagePage(getDriver());
    }

    public LanguagePage clickAddTeamMember() {
        log.info("Click Add Team Member");
        clickElement(addTeamMemberButton);
        return new LanguagePage(getDriver());
    }
    /*
     * Convenience function for adding a language team member
     */

    public LanguagePage searchPersonAndAddToTeam(final String personName,
            TeamPermission... permissions) {
        // Convenience!
        enterUsername(personName);
        clickSearch();
        clickAddUserRoles(personName, permissions);
        clickAddSelectedButton();
        return confirmAdded(personName);
    }

    private LanguagePage enterUsername(String username) {
        log.info("Enter username search {}", username);
        WebElement addUserField = readyElement(addUserSearchInput);
        touchTextField(addUserField);
        enterText(addUserField, username);
        return new LanguagePage(getDriver());
    }

    private LanguagePage clickSearch() {
        log.info("Click Search");
        clickElement(addUserSearchButton);
        return new LanguagePage(getDriver());
    }

    private LanguagePage clickAddUserRoles(final String username,
            TeamPermission... permissions) {
        log.info("Click user permissions");
        // if permissions is empty, default add as translator
        Set<TeamPermission> permissionToAdd = Sets.newHashSet(permissions);
        permissionToAdd.add(TeamPermission.Translator);
        for (final TeamPermission permission : permissionToAdd) {
            log.info("Set checked as {}", permission.name());
            waitForAMoment().until((Predicate<WebDriver>) webDriver -> {
                WebElement inputDiv = getSearchedForUser(username)
                        .findElement(By.className("list--horizontal"))
                        .findElements(By.tagName("li"))
                        .get(permission.columnIndex)
                        .findElement(By.className("form__checkbox"));
                WebElement input = inputDiv.findElement(By.tagName("input"));
                Checkbox checkbox = Checkbox.of(input);
                if (!checkbox.checked()) {
                    inputDiv.click();
                    waitForPageSilence();
                }
                return checkbox.checked();
            });
        }
        return new LanguagePage(getDriver());
    }

    private LanguagePage confirmAdded(final String personUsername) {
        // we need to wait for the page to refresh
        refreshPageUntil(this, (Predicate<WebDriver>) driver -> {
            return getMemberUsernames().contains(personUsername);
        }, "Wait for names to contain " + personUsername);
        return new LanguagePage(getDriver());
    }

    private WebElement getSearchedForUser(final String username) {
        return waitForAMoment()
                .until((Function<WebDriver, WebElement>) webDriver -> {
                    WebElement list = readyElement(personTable)
                            .findElement(By.className("list--slat"));
                    List<WebElement> rows =
                            list.findElements(By.className("txt--meta"));
                    rows.addAll(list.findElements(By.className("txt--mini")));
                    for (WebElement row : rows) {
                        if (getListItemUsername(row).equals(username)) {
                            return row;
                        }
                    }
                    return null;
                });
    }

    private String getListItemUsername(WebElement listItem) {
        String fullname =
                listItem.findElements(By.className("g__item")).get(0).getText();
        return fullname.substring(fullname.indexOf('[') + 1,
                fullname.indexOf(']'));
    }

    public LanguagePage clickAddSelectedButton() {
        log.info("Click Add Selected");
        clickElement(addSelectedButton);
        return new LanguagePage(getDriver());
    }

    public static enum TeamPermission {
        Translator(IS_TRANSLATOR_COLUMN),
        Reviewer(IS_REVIEWER_COLUMN),
        Coordinator(IS_COORDINATOR_COLUMN);
        private final int columnIndex;

        TeamPermission(int columnIndex) {
            this.columnIndex = columnIndex;
        }
    }
}
