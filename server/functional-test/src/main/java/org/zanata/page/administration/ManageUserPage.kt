/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.administration

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.BasePage

import java.util.ArrayList

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ManageUserPage(driver: WebDriver) : BasePage(driver) {

    private val userTable = By.id("usermanagerForm")
    private val lockIcon = By.className("i--lock")
    private val listEntry = By.className("list__item--actionable")
    private val actionsDropdown = By.id("rolemanage-more-actions")
    private val createUser = By.linkText("Create new user")

    /**
     * Get a list of user names
     * @return String list of user names
     */
    // Page may refresh user list
    val userList: List<String>
        get() {
            log.info("Query user list")
            waitForPageSilence()
            val names = ArrayList<String>()
            for (element in rows) {
                names.add(getListItemUsername(element))
            }
            return names
        }

    // Retrieve all of the user rows from the table
    private val rows: List<WebElement>
        get() = readyElement(userTable).findElements(listEntry)

    /**
     * Press the edit button on a user account
     * @param username of account to edit
     * @return new ManageUserAccountPage
     */
    fun editUserAccount(username: String): ManageUserAccountPage {
        log.info("Click edit on {}", username)
        clickElement(findRowByUserName(username))
        return ManageUserAccountPage(driver)
    }

    /**
     * Open the menu and select Create New User
     * @return new CreateUserAccountPage
     */
    fun selectCreateNewUser(): CreateUserAccountPage {
        log.info("Click Create new user")
        clickElement(actionsDropdown)
        clickLinkAfterAnimation(createUser)
        return CreateUserAccountPage(driver)
    }

    /**
     * Query if user is enabled
     * @param username to query
     * @return boolean user is enabled
     */
    fun isUserEnabled(username: String): Boolean {
        log.info("Query is user {} enabled", username)
        return findRowByUserName(username).findElements(lockIcon).isEmpty()
    }

    // Find the row WebElement containing the username
    private fun findRowByUserName(username: String): WebElement {
        for (listItem in rows) {
            if (getListItemUsername(listItem) == username) {
                // TODO this is ugly but seems to work in firefox
                val linksUnderneath = listItem.findElements(By.tagName("a"))
                for (link in linksUnderneath) {
                    val onclickCallback = link.getAttribute("href")
                    if (onclickCallback != null && onclickCallback.contains("/userdetail?")) {
                        return link
                    }
                }
                return listItem
            }
        }
        throw RuntimeException("Search for username " + username + " failed")
    }

    // Retrieve the username from a user row
    private fun getListItemUsername(listItem: WebElement): String {
        val listItemText = listItem.findElement(By.tagName("h3")).text
        return listItemText
                .substring(0, listItemText.lastIndexOf(getListItemRoles(listItem)))
                .trim { it <= ' ' }
    }

    // Retrieve the list of roles, as a string, from a user row
    private fun getListItemRoles(listItem: WebElement): String {
        return listItem.findElement(By.tagName("h3"))
                .findElement(By.className("txt--meta")).text
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ManageUserPage::class.java)
    }

}
