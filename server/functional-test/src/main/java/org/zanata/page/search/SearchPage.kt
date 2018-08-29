/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.search

import java.util.ArrayList
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage

@Suppress("unused")
class SearchPage(driver: WebDriver) : BasePage(driver) {
    private val searchProjectForm = By.id("search-project_form")
    private val projectTabSearchPage = By.id("projects_tab")
    private val userTabSearchPage = By.id("users_tab")

    val projectNamesOnSearchPage: List<String>
        get() {
            log.info("Query Projects list")
            waitForPageSilence()
            gotoProjectTabInSearchPage()
            log.info("Query project name")
            val names = ArrayList<String>()
            for (row in readyElement(searchProjectForm)
                    .findElements(By.xpath("//h3[@class=\'list__title\']"))) {
                names.add(row.text)
            }
            return names
        }

    private fun gotoProjectTabInSearchPage() {
        clickElement(projectTabSearchPage)
    }

    fun gotoUserTabInSearchPage() {
        clickElement(userTabSearchPage)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(SearchPage::class.java)
    }
}
