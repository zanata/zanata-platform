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
package org.zanata.page.projects

import org.assertj.core.api.Assertions.assertThat

import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.zanata.page.projectversion.CreateVersionPage
import org.zanata.page.projectversion.VersionLanguagesPage
import org.zanata.util.WebElementUtil

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ProjectVersionsPage(driver: WebDriver) : ProjectBasePage(driver) {
    private val versionTabMoreAction = By.id("versions-more-actions")
    private val createNewVersion = By.id("new-version-link")
    private val versionCount = By.id("versionSearch:versionSearch-page-info")
    private val versionsList = By.id("versions")
    private val searchIcon = By.className("panel__search__button")
    private val versionSearchInput = By.id("versionSearch__input")

    val numberOfDisplayedVersions: Int
        get() {
            log.info("Query number of displayed versions")
            return Integer.parseInt(readyElement(versionCount).text)
        }

    fun clickCreateVersionLink(): CreateVersionPage {
        log.info("Click Create Version")
        gotoVersionsTab()
        clickLinkAfterAnimation(versionTabMoreAction)
        clickLinkAfterAnimation(createNewVersion)
        return CreateVersionPage(driver)
    }

    fun gotoVersion(versionId: String): VersionLanguagesPage {
        log.info("Click Version {}", versionId)
        waitForAMoment().withMessage("click on version in list").until {
            clickElement(By.id("versions_tab"))
            val versionLinks = existingElement(By.id("versions_form"))
                    .findElement(By.className("list--stats"))
                    .findElements(By.tagName("li"))
            var clicked = false
            for (links in versionLinks) {
                // The Translate Options menu can get picked up here
                for (link in links.findElements(By.tagName("a"))) {
                    if (link.text.contains(versionId)) {
                        link.click()
                        clicked = true
                        break
                    }
                }
                if (clicked)
                    break
            }
            clicked
        }
        return VersionLanguagesPage(driver)
    }

    fun getVersions(): List<String> {
        log.info("Query Versions list")
        return WebElementUtil.elementsToText(driver,
                By.xpath("//h3[@class=\'list__title\']"))
    }

    fun expectDisplayedVersions(expected: Int): ProjectVersionsPage {
        log.info("Wait for number of displayed versions to be {}", expected)
        waitForPageSilence()
        waitForAMoment().withMessage("Waiting for versions").until {
            numberOfDisplayedVersions == expected
        }
        assertThat(numberOfDisplayedVersions).isEqualTo(expected)
        assertThat(getVersions()).hasSize(expected)
        return ProjectVersionsPage(driver)
    }

    fun clickSearchIcon(): ProjectVersionsPage {
        log.info("Click Search icon")
        clickElement(readyElement(existingElement(versionsList), searchIcon))
        return ProjectVersionsPage(driver)
    }

    fun clearVersionSearch(): ProjectVersionsPage {
        log.info("Clear version search field")
        var maxKeys = 500
        while (!readyElement(versionSearchInput).getAttribute("value").isEmpty() && maxKeys > 0) {
            readyElement(versionSearchInput).sendKeys(Keys.BACK_SPACE)
            maxKeys -= 1
        }
        if (maxKeys == 0) {
            log.warn("Exceeded max keypresses for clearing search bar")
        }
        return ProjectVersionsPage(driver)
    }

    fun enterVersionSearch(searchTerm: String): ProjectVersionsPage {
        log.info("Enter version search {}", searchTerm)
        enterText(readyElement(versionSearchInput), searchTerm)
        return ProjectVersionsPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectVersionsPage::class.java)
    }
}
