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
package org.zanata.page.account

import org.openqa.selenium.By
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ProfilePage(driver: WebDriver) : BasePage(driver) {
    private val displayNameBy = By.id("profile-displayname")
    private val userNameBy = By.id("profile-username")
    private val languagesBy = By.id("profileLanguages")
    private val contributionsBy = By.id("userProfile-matrix")

    /**
     * Get the display name of the user
     * @return name string
     */
    val displayName: String
        get() {
            log.info("Query user display name")
            return getText(displayNameBy)
        }

    /**
     * Get the username of the user
     * @return username string
     */
    val username: String
        get() {
            log.info("Query user username")
            return getText(userNameBy)
        }

    /**
     * Get the languages of the user
     * @return languages string
     */
    val languages: String
        get() {
            log.info("Query user languages list")
            return getText(languagesBy)
        }

    /**
     * Wait for the contributions matrix to be displayed
     * @return successful display of the matrix
     */
    fun expectContributionsMatrixVisible(): Boolean {
        log.info("Wait for contributions matrix to be visible")
        try {
            waitForAMoment().withMessage("displayed contributions matrix")
                    .until { driver ->
                        driver.findElements(contributionsBy).size > 0
                    }
        } catch (te: TimeoutException) {
            log.info("Contributions matrix was not discovered")
            return false
        }

        return true
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProfilePage::class.java)
    }
}
