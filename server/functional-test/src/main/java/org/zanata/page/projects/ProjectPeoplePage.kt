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

import java.util.ArrayList

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.util.WebElementUtil

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ProjectPeoplePage(driver: WebDriver) : ProjectBasePage(driver) {
    private val peopleList = By.id("people_form")
    private val addSomeoneForm = By.id("project-people_add")
    private val addSomeoneInput = By.id("modalManagePermissionsAutocomplete")

    val people: List<String>
        get() {
            log.info("Query people list")
            val names = ArrayList<String>()
            for (row in readyElement(peopleList)
                    .findElements(By.tagName("li"))) {
                val username = row.findElement(By.tagName("a")).text.trim { it <= ' ' }
                var roles = ""
                for (role in row
                        .findElements(By.className("txt--understated"))) {
                    roles += (role.text.trim { it <= ' ' } + ";")
                }
                names.add("$username|$roles")
            }
            return names
        }

    fun clickAddSomeone(): ProjectPeoplePage {
        log.info("Click Add Someone button")
        clickElement(existingElement(addSomeoneForm).findElement(By.tagName("button")))
        return ProjectPeoplePage(driver)
    }

    fun enterAddSomeoneUsername(username: String): ProjectPeoplePage {
        log.info("Enter user's username to search for")
        enterText(existingElement(addSomeoneInput), username, true, false, false)
        return ProjectPeoplePage(driver)
    }

    fun selectUserFromAddList(username: String): ProjectPeoplePage {
        log.info("Click project version {}", username)
        waitForAMoment().withMessage("click on username in list")
                .until { driver ->
                    val items = WebElementUtil.getSearchAutocompleteResults(driver,
                            "peopleTab-permissions", "modalManagePermissionsAutocomplete")
                    for (item in items) {
                        if (item.text == username) {
                            item.click()
                            return@until true
                        }
                    }
                    false
                }
        return ProjectPeoplePage(driver)
    }

    fun clickTranslatorCheckboxFor(language: String): ProjectPeoplePage {
        log.info("Click checkbox for translator: {}", language)
        waitForAMoment().withMessage("click language checkbox").until {
            var found = false
            val items = existingElement(By.id("peopleTab-permissions"))
                    .findElement(By.className("list--slat")).findElements(By.tagName("li"))
            for (item in items) {
                if (item.findElement(By.tagName("label")).text.trim { it <= ' ' } == language) {
                    found = true
                    clickElement(existingElement(item, By.className("form__checkbox")))
                    break
                }
            }
            found
        }
        slightPause()
        return ProjectPeoplePage(driver)
    }

    fun clickAddPerson(): ProjectPeoplePage {
        log.info("Click Add Person button")
        clickElement(existingElement(
                By.id("peopleTab-permissions:modalManagePermissions-submit-buttons"))
                .findElement(By.tagName("input")))
        slightPause()
        return ProjectPeoplePage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectPeoplePage::class.java)
    }
}
