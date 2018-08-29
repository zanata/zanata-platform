/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
import org.zanata.page.BasePage

class AdministrationPage(driver: WebDriver) : BasePage(driver) {

    private val configureServerLink = By.id("Admin_Server_configuration_home")
    private val manageUserLink = By.id("Admin_Manage_users_home")
    private val manageTMLink = By.id("Translation_Memory_home")
    private val manageSearchLink = By.id("Admin_Manage_search_home")
    private val manageRolesAssignmentLink = By.id("Admin_Role_Assignment_Rules_home")

    /**
     * Click the Server Configuration button
     * @return new ServerConfigurationPage
     */
    fun goToServerConfigPage(): ServerConfigurationPage {
        log.info("Click Server Configuration")
        clickLinkAfterAnimation(configureServerLink)
        return ServerConfigurationPage(driver)
    }

    /**
     * Click the Manage Users button
     * @return new ManageUserPage
     */
    fun goToManageUserPage(): ManageUserPage {
        log.info("Click Manage Users")
        clickLinkAfterAnimation(manageUserLink)
        return ManageUserPage(driver)
    }

    /**
     * Click the Translation Memory button
     * @return new TranslationMemoryPage
     */
    fun goToTranslationMemoryPage(): TranslationMemoryPage {
        log.info("Click Translation Memory")
        clickLinkAfterAnimation(manageTMLink)
        return TranslationMemoryPage(driver)
    }

    /**
     * Click the Manage Search button
     * @return new ManageSearchPage
     */
    fun goToManageSeachPage(): ManageSearchPage {
        log.info("Click Manage Search")
        clickLinkAfterAnimation(manageSearchLink)
        return ManageSearchPage(driver)
    }

    /**
     * Click the Manage Roles button
     * @return new RoleAssignmentsPage
     */
    fun goToManageRoleAssignments(): RoleAssignmentsPage {
        log.info("Click Manage Roles")
        clickLinkAfterAnimation(manageRolesAssignmentLink)
        return RoleAssignmentsPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(AdministrationPage::class.java)
    }
}
