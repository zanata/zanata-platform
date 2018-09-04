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
package org.zanata.page.languages

import java.util.ArrayList

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.BasePage
import org.zanata.util.Checkbox
import com.google.common.collect.Sets

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class LanguagePage(driver: WebDriver) : BasePage(driver) {
    private val contactCoordinatorsButton = By.id("contact-coordinator")
    private val saveButton = By.id("settings-general_form:save-language-settings")
    private val moreActions = By.id("more-action")
    private val enableByDefault = By.id("settings-general_form:enable-by-default")
    private val membersTab = By.id("members_tab")
    private val settingsTab = By.id("settings_tab")
    private val joinLanguageTeamButton = By.xpath(".//span[contains(text(),'Join Team')]")
    private val addTeamMemberButton = By.id("add-team-member-button")
    private val addUserSearchInput = By.id("searchForm:searchField")
    private val addUserSearchButton = By.id("searchForm:searchBtn")
    private val personTable = By.id("resultForm:searchResults")
    private val addSelectedButton = By.id("addSelectedBtn")
    private val requestToJoinLanguage = By.xpath(".//button[contains(text(),'Request To Join')]")
    private val cancelRequest = By.xpath(".//span[contains(text(),'Cancel request')]")
    private val leaveLanguageTeam = By.xpath(".//span[contains(text(),'Leave Team')]")

    val memberUsernames: List<String>
        get() {
            log.info("Query username list")
            if (memberCount == "0") {
                log.info("No members yet for this language")
                return emptyList()
            }
            val names = ArrayList<String>()
            val form = existingElement(By.id("members-form"))
            for (listEntry in form
                    .findElements(By.className("list__item--actionable"))) {
                names.add(listEntry.findElement(By.className("list__item__info"))
                        .text.trim { it <= ' ' })
            }
            log.info("Found {}", names)
            return names
        }

    private val memberCount: String
        get() {
            log.info("Query members info")
            return readyElement(By.id("members-size")).text.trim { it <= ' ' }
        }

    fun clickMoreActions(): LanguagePage {
        log.info("Click More Actions")
        clickElement(moreActions)
        return LanguagePage(driver)
    }

    fun clickContactCoordinatorsButton(): ContactTeamPage {
        log.info("Click Contact Coordinators button")
        clickElement(contactCoordinatorsButton)
        return ContactTeamPage(driver)
    }

    fun gotoSettingsTab(): LanguagePage {
        clickElement(settingsTab)
        return LanguagePage(driver)
    }

    fun gotoMembersTab(): LanguagePage {
        clickElement(membersTab)
        return LanguagePage(driver)
    }

    @Suppress("unused")
    fun enableLanguageByDefault(enable: Boolean): LanguagePage {
        val checkbox = Checkbox.of(existingElement(enableByDefault)
                .findElement(By.className("form__checkbox__item")))
        if (enable) {
            checkbox.check()
        } else {
            checkbox.uncheck()
        }
        return LanguagePage(driver)
    }

    fun saveSettings(): LanguagePage {
        clickElement(saveButton)
        return LanguagePage(driver)
    }

    fun joinLanguageTeam(): LanguagePage {
        log.info("Click Join")
        clickElement(joinLanguageTeamButton)
        // we need to wait for this join to finish before returning the page
        waitForAMoment().withMessage("join language button is not displayed")
                .until { driver -> driver.findElements(joinLanguageTeamButton).isEmpty() }
        return LanguagePage(driver)
    }

    fun clickAddTeamMember(): LanguagePage {
        log.info("Click Add Team Member")
        clickElement(addTeamMemberButton)
        return LanguagePage(driver)
    }
    /*
     * Convenience function for adding a language team member
     */

    fun searchPersonAndAddToTeam(personName: String,
                                 vararg permissions: TeamPermission): LanguagePage {
        // Convenience!
        enterUsername(personName)
        clickSearch()
        clickAddUserRoles(personName, *permissions)
        clickAddSelectedButton()
        return confirmAdded(personName)
    }

    private fun enterUsername(username: String): LanguagePage {
        log.info("Enter username search {}", username)
        val addUserField = readyElement(addUserSearchInput)
        touchTextField(addUserField)
        enterText(addUserField, username)
        return LanguagePage(driver)
    }

    private fun clickSearch(): LanguagePage {
        log.info("Click Search")
        clickElement(addUserSearchButton)
        return LanguagePage(driver)
    }

    private fun clickAddUserRoles(username: String,
                                  vararg permissions: TeamPermission): LanguagePage {
        log.info("Click user permissions")
        // if permissions is empty, default add as translator
        val permissionToAdd = Sets.newHashSet(*permissions)
        permissionToAdd.add(TeamPermission.Translator)
        for (permission in permissionToAdd) {
            log.info("Set checked as {}", permission.name)
            waitForAMoment().withMessage("click user role " + permission.name)
                    .until {
                        val inputDiv = getSearchedForUser(username)
                                .findElement(By.className("list--horizontal"))
                                .findElements(By.tagName("li"))[permission.columnIndex]
                                .findElement(By.className("form__checkbox"))
                        val input = inputDiv.findElement(By.tagName("input"))
                        val checkbox = Checkbox.of(input)
                        if (!checkbox.checked()) {
                            inputDiv.click()
                            waitForPageSilence()
                        }
                        checkbox.checked()
                    }
        }
        return LanguagePage(driver)
    }

    private fun confirmAdded(personUsername: String): LanguagePage {
        // we need to wait for the page to refresh
        refreshPageUntil(this, "Wait for names to contain $personUsername")
        { memberUsernames.contains(personUsername) }
        return LanguagePage(driver)
    }

    private fun getSearchedForUser(username: String): WebElement {
        return waitForAMoment().withMessage("get user row")
                .until<WebElement> {
                    val list = readyElement(personTable)
                            .findElement(By.className("list--slat"))
                    val rows = list.findElements(By.className("txt--meta"))
                    rows.addAll(list.findElements(By.className("txt--mini")))
                    for (row in rows) {
                        if (getListItemUsername(row) == username) {
                            return@until row
                        }
                    }
                    null
                }
    }

    private fun getListItemUsername(listItem: WebElement): String {
        val fullname = listItem.findElements(By.className("g__item"))[0].text
        return fullname.substring(fullname.indexOf('[') + 1,
                fullname.indexOf(']'))
    }

    private fun clickAddSelectedButton(): LanguagePage {
        log.info("Click Add Selected")
        clickElement(addSelectedButton)
        return LanguagePage(driver)
    }

    enum class TeamPermission(val columnIndex: Int) {
        Translator(IS_TRANSLATOR_COLUMN),
        Reviewer(IS_REVIEWER_COLUMN),
        Coordinator(IS_COORDINATOR_COLUMN)
    }

    fun requestToJoin(): RequestToJoinPopup {
        log.info("Click Request To Join")
        clickElement(requestToJoinLanguage)
        return RequestToJoinPopup(driver)
    }

    fun leaveTeam(): LanguagePage {
        log.info("Click Leave Team")
        clickElement(leaveLanguageTeam)
        return LanguagePage(driver)
    }

    fun cancelRequest(): LanguagePage {
        log.info("Click Cancel Request")
        clickElement(cancelRequest)
        return LanguagePage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(LanguagePage::class.java)
        const val IS_TRANSLATOR_COLUMN = 0
        const val IS_REVIEWER_COLUMN = 1
        const val IS_COORDINATOR_COLUMN = 2
    }

}
