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
package org.zanata.page.projects.projectsettings


import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.projects.ProjectBasePage
import org.zanata.util.WebElementUtil
import org.assertj.core.api.Assertions.assertThat
import kotlin.streams.toList

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ProjectPermissionsTab(driver: WebDriver) : ProjectBasePage(driver) {
    private val maintainersForm = By.id("settings-permissions-form")

    private val settingsMaintainersElement: List<WebElement>
        get() = readyElement(maintainersForm)
                .findElement(By.id("maintainers-list"))
                .findElements(By.className("reveal--list-item"))

    val settingsMaintainersList: List<String>
        get() {
            log.info("Query maintainers list")
            val items = settingsMaintainersElement
            return items.stream().map<String> { it -> this.getUsername(it) }.toList()
        }

    fun enterSearchMaintainer(maintainerQuery: String): ProjectPermissionsTab {
        log.info("Enter user search {}", maintainerQuery)
        WebElementUtil.searchAutocomplete(driver, "maintainerAutocomplete",
                maintainerQuery)
        return ProjectPermissionsTab(driver)
    }

    fun selectSearchMaintainer(maintainer: String): ProjectPermissionsTab {
        log.info("Select user {}", maintainer)
        waitForAMoment().withMessage("click on maintainer user")
                .until { driver ->
                    val searchResults = WebElementUtil.getSearchAutocompleteResults(driver,
                            "settings-permissions-form",
                            "maintainerAutocomplete")
                    var clickedUser = false
                    for (searchResult in searchResults) {
                        if (searchResult.text.contains(maintainer)) {
                            searchResult.click()
                            clickedUser = true
                            break
                        }
                    }
                    clickedUser
                }
        return ProjectPermissionsTab(driver)
    }

    fun clickRemoveOn(maintainer: String): ProjectPermissionsTab {
        log.info("Click Remove on {}", maintainer)
        clickElement(getMaintainerElementFromList(maintainer).findElement(By.tagName("a")))
        return ProjectPermissionsTab(driver)
    }

    fun clickRemoveOnSelf(maintainer: String): ProjectBasePage {
        log.info("Click Remove on (self) {}", maintainer)
        clickElement(getMaintainerElementFromList(maintainer).findElement(By.tagName("a")))
        return ProjectBasePage(driver)
    }

    private fun getUsername(maintainersLi: WebElement): String {
        return maintainersLi.findElement(By.className("txt--meta")).text
                .replace("@", "")
    }

    private fun getMaintainerElementFromList(maintainer: String): WebElement {
        return waitForAMoment().withMessage("get maintainer $maintainer")
                .until<WebElement> {
                    for (maintainersLi in settingsMaintainersElement) {
                        val displayedUsername = getUsername(maintainersLi)
                        if (displayedUsername == maintainer) {
                            return@until maintainersLi
                        }
                    }
                    null
                }
    }

    fun expectMaintainersContains(username: String): ProjectPermissionsTab {
        log.info("Wait for maintainers contains {}", username)
        waitForPageSilence()
        assertThat(settingsMaintainersList).contains(username)
        return ProjectPermissionsTab(driver)
    }

    fun expectMaintainersNotContains(username: String): ProjectPermissionsTab {
        log.info("Wait for maintainers does not contain {}", username)
        waitForPageSilence()
        assertThat(settingsMaintainersList).doesNotContain(username)
        return ProjectPermissionsTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectPermissionsTab::class.java)
    }
}
