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
package org.zanata.page.administration

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.Select
import org.zanata.page.BasePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class EditRoleAssignmentPage(driver: WebDriver) : BasePage(driver) {

    private val policySelect = By.id("role-rule-form:policyName:input:policyName")
    private val patternField = By.id("role-rule-form:identityPattern:input:identityPattern")
    private val roleSelect = By.id("role-rule-form:role:input:roles")
    private val saveButton = By.id("role-rule-form:save")
    private val cancelButton = By.id("role-rule-form:cancel")

    /**
     * Select a role assignment policy
     * @param policy string to select
     * @return new EditRoleAssignmentPage
     */
    @Suppress("unused")
    fun selectPolicy(policy: String): EditRoleAssignmentPage {
        log.info("Select policy {}", policy)
        Select(readyElement(policySelect)).selectByValue(policy)
        return EditRoleAssignmentPage(driver)
    }

    /**
     * Enter an identity pattern for matching a username
     * @param pattern string to enter
     * @return new EditRoleAssignmentPage
     */
    fun enterIdentityPattern(pattern: String): EditRoleAssignmentPage {
        log.info("Enter identity pattern {}", pattern)
        readyElement(patternField).clear()
        enterText(patternField, pattern)
        return EditRoleAssignmentPage(driver)
    }

    /**
     * Select a role to assign
     * @param role string to select
     * @return new EditRoleAssignmentPage
     */
    fun selectRole(role: String): EditRoleAssignmentPage {
        log.info("Select role {}", role)
        Select(readyElement(roleSelect)).selectByValue(role)
        return EditRoleAssignmentPage(driver)
    }

    /**
     * Press the Save button
     * @return new RoleAssignmentsPage
     */
    fun saveRoleAssignment(): RoleAssignmentsPage {
        log.info("Click Save")
        clickElement(saveButton)
        return RoleAssignmentsPage(driver)
    }

    /**
     * Press the Cancel button
     * @return new RoleAssignmentsPage
     */
    @Suppress("unused")
    fun cancelEditRoleAssignment(): RoleAssignmentsPage {
        log.info("Click Cancel")
        clickElement(cancelButton)
        return RoleAssignmentsPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(EditRoleAssignmentPage::class.java)
    }
}
