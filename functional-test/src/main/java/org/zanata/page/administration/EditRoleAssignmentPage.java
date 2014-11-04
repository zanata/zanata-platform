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
package org.zanata.page.administration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class EditRoleAssignmentPage extends BasePage {

    private By policySelect = By.id("projectForm:policyNameField:policyName");
    private By patternField = By.id("projectForm:identityPatternField:identityPattern");
    private By roleSelect = By.id("projectForm:roleField:roles");
    private By saveButton = By.id("projectForm:save");
    private By cancelButton = By.id("projectForm:cancel");

    public EditRoleAssignmentPage(WebDriver driver) {
        super(driver);
    }

    public EditRoleAssignmentPage selectPolicy(String policy) {
        log.info("Select policy {}", policy);
        new Select(waitForWebElement(policySelect)).selectByValue(policy);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage enterIdentityPattern(String pattern) {
        log.info("Enter identity pattern {}", pattern);
        waitForWebElement(patternField).clear();
        waitForWebElement(patternField).sendKeys(pattern);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage selectRole(String role) {
        log.info("Select role {}", role);
        new Select(waitForWebElement(roleSelect)).selectByValue(role);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage saveRoleAssignment() {
        log.info("Click Save");
        waitForWebElement(saveButton).click();
        return new EditRoleAssignmentPage(getDriver());
    }

    public RoleAssignmentsPage cancelEditRoleAssignment() {
        log.info("Click Cancel");
        waitForWebElement(cancelButton).click();
        return new RoleAssignmentsPage(getDriver());
    }
}
