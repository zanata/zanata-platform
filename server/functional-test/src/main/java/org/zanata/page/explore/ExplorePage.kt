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
package org.zanata.page.explore

import org.assertj.core.api.Assertions.assertThat
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.BasePage
import org.zanata.page.account.ProfilePage
import org.zanata.page.groups.VersionGroupPage
import org.zanata.page.languages.LanguagePage
import org.zanata.page.projects.ProjectVersionsPage

import com.google.common.collect.Lists

/**
 * @author Alex Eng[aeng@redhat.com](mailto:aeng@redhat.com)
 */
class ExplorePage(driver: WebDriver) : BasePage(driver) {

    val isCancelButtonEnabled: Boolean
        get() {
            log.info("Query cancel button is enabled")
            return readyElement(searchInput).isEnabled
        }

    val isSearchFieldCleared: Boolean
        get() {
            log.info("Query is search field clear")
            return readyElement(searchInput).text == ""
        }

    val projectSearchResults: List<String>
        get() {
            log.info("Query Project search results list")
            return getResultText(projectResult)
        }

    val groupSearchResults: List<String>
        get() {
            log.info("Query Group search results list")
            return getResultText(groupResult)
        }

    val userSearchResults: List<String>
        get() {
            log.info("Query User search results list")
            return getResultText(personResult)
        }

    val languageSearchResults: List<String>
        get() {
            log.info("Query Language search results list")
            return getResultText(languageTeamResult)
        }

    private val isProjectSearchLoading: Boolean
        get() = isSearchLoading(projectResult)

    private val isGroupSearchLoading: Boolean
        get() = isSearchLoading(groupResult)

    private val isPersonSearchLoading: Boolean
        get() = isSearchLoading(personResult)

    private val isLanguageTeamSearchLoading: Boolean
        get() = isSearchLoading(languageTeamResult)

    fun clearSearch(): ExplorePage {
        log.info("Clear search field")
        readyElement(searchInput).clear()
        return ExplorePage(driver)
    }

    fun enterSearch(searchText: String): ExplorePage {
        log.info("Enter Explore search {}", searchText)
        existingElement(searchInput).sendKeys(searchText)
        waitForAMoment().withMessage("Waiting for search complete").until {
            (!isProjectSearchLoading
                    && !isGroupSearchLoading
                    && !isLanguageTeamSearchLoading
                    && !isPersonSearchLoading)
        }
        return ExplorePage(driver)
    }

    fun expectProjectListContains(expected: String): ExplorePage {
        val msg = "Project search list contains $expected"
        log.info("Expect {}", msg)
        waitForAMoment().withMessage("Waiting for search contains")
                .until { projectSearchResults.contains(expected) }
        assertThat(projectSearchResults).describedAs(msg).contains(expected)
        return ExplorePage(driver)
    }

    fun expectGroupListContains(expected: String): ExplorePage {
        val msg = "Group search list contains $expected"
        log.info("Expect {}", msg)
        waitForAMoment().withMessage("Waiting for search contains")
                .until { groupSearchResults.contains(expected) }
        assertThat(groupSearchResults).describedAs(msg).contains(expected)
        return ExplorePage(driver)
    }

    fun expectPersonListContains(expected: String): ExplorePage {
        waitForPageSilence()
        val msg = "Person search list contains $expected"
        log.info("Expect {}", msg)
        waitForAMoment().withMessage("Waiting for search contains")
                .until { userSearchResults.contains(expected) }
        assertThat(userSearchResults).describedAs(msg).contains(expected)
        return ExplorePage(driver)
    }

    fun expectLanguageTeamListContains(expected: String): ExplorePage {
        val msg = "Language Team search list contains $expected"
        log.info("Expect {}", msg)
        waitForAMoment().withMessage("Waiting for search contains")
                .until { languageSearchResults.contains(expected) }
        assertThat(languageSearchResults).describedAs(msg).contains(expected)
        return ExplorePage(driver)
    }

    private fun isSearchLoading(by: By): Boolean {
        return !existingElement(by).findElements(By.name("loader")).isEmpty()
    }

    private fun getResultText(by: By): List<String> {
        val entries = existingElement(by).findElements(By.name("entry"))
        val list = Lists.newArrayList<String>()
        for (element in entries) {
            val aTag = element.findElement(By.tagName("a"))
            if (aTag != null) {
                list.add(aTag.text)
            }
        }
        return list
    }

    fun clickUserSearchEntry(searchEntry: String): ProfilePage {
        log.info("Click user search result {}", searchEntry)
        val users = existingElement(personResult).findElements(By.name("entry"))
        for (element in users) {
            val aTag = element.findElement(By.tagName("a"))
            if (aTag != null && aTag.text == searchEntry) {
                clickElement(aTag)
                break
            }
        }
        return ProfilePage(driver)
    }

    fun clickProjectEntry(searchEntry: String): ProjectVersionsPage {
        log.info("Click Projects search result {}", searchEntry)
        val projects = existingElement(projectResult).findElements(By.name("entry"))
        for (element in projects) {
            val aTag = element.findElement(By.tagName("a"))
            if (aTag != null && aTag.text == searchEntry) {
                clickElement(aTag)
                break
            }
        }
        return ProjectVersionsPage(driver)
    }

    fun clickGroupSearchEntry(searchEntry: String): VersionGroupPage {
        log.info("Click group search result {}", searchEntry)
        val groups = existingElement(groupResult).findElements(By.name("entry"))
        for (element in groups) {
            val aTag = element.findElement(By.tagName("a"))
            if (aTag != null && aTag.text == searchEntry) {
                clickElement(aTag)
                break
            }
        }
        return VersionGroupPage(driver)
    }

    @Suppress("unused")
    fun clickLangSearchEntry(searchEntry: String): LanguagePage {
        log.info("Click language search result {}", searchEntry)
        val langs = existingElement(groupResult).findElements(By.name("entry"))
        for (element in langs) {
            val aTag = element.findElement(By.tagName("a"))
            if (aTag != null && aTag.text == searchEntry) {
                clickElement(aTag)
                break
            }
        }
        return LanguagePage(driver)
    }

    fun searchAndGotoProjectByName(projectName: String): ProjectVersionsPage {
        log.info("go to project by name with name: {}", projectName)
        return if (projectSearchResults.contains(projectName)) {
            clickProjectEntry(projectName)
        } else {
            enterSearch(projectName).clickProjectEntry(projectName)
        }
    }

    @Suppress("unused")
    fun searchAndGotoGroupByName(groupName: String): VersionGroupPage {
        log.info("Go to project by name with name: {}", groupName)
        return if (groupSearchResults.contains(groupName)) {
            clickGroupSearchEntry(groupName)
        } else {
            enterSearch(groupName).clickGroupSearchEntry(groupName)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ExplorePage::class.java)
        private val searchInput = By.id("explore_search")
        private val projectResult = By.id("explore_Project_result")
        private val personResult = By.id("explore_Person_result")
        private val groupResult = By.id("explore_Group_result")
        private val languageTeamResult = By.id("explore_LanguageTeam_result")
    }

}
