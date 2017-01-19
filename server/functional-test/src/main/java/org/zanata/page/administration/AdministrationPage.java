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
package org.zanata.page.administration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;

public class AdministrationPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AdministrationPage.class);
    private final By CONFIGURE_SERVER_LINK =
            By.id("Admin_Server_configuration_home");
    private final By MANAGE_LANGUAGE_LINK =
            By.id("Admin_Manage_languages_home");
    private final By MANAGE_USER_LINK = By.id("Admin_Manage_users_home");
    private final By MANAGE_TM_LINK = By.id("Translation_Memory_home");
    private final By MANAGE_SEARCH_LINK = By.id("Admin_Manage_search_home");
    private final By MANAGE_ROLES_ASSIGN_LINK =
            By.id("Admin_Role_Assignment_Rules_home");

    public AdministrationPage(WebDriver driver) {
        super(driver);
    }

    public ServerConfigurationPage goToServerConfigPage() {
        log.info("Click Server Configuration");
        clickLinkAfterAnimation(CONFIGURE_SERVER_LINK);
        return new ServerConfigurationPage(getDriver());
    }

    public ManageUserPage goToManageUserPage() {
        log.info("Click Manage Users");
        clickLinkAfterAnimation(MANAGE_USER_LINK);
        return new ManageUserPage(getDriver());
    }

    public TranslationMemoryPage goToTranslationMemoryPage() {
        log.info("Click Translation Memory");
        clickLinkAfterAnimation(MANAGE_TM_LINK);
        return new TranslationMemoryPage(getDriver());
    }

    public ManageSearchPage goToManageSeachPage() {
        clickLinkAfterAnimation(MANAGE_SEARCH_LINK);
        return new ManageSearchPage(getDriver());
    }

    public RoleAssignmentsPage goToManageRoleAssignments() {
        log.info("Click Manage Roles");
        clickLinkAfterAnimation(MANAGE_ROLES_ASSIGN_LINK);
        return new RoleAssignmentsPage(getDriver());
    }
}
