/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class CreateUserAccountPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CreateUserAccountPage.class);

    private By usernameField = By.id("newUserForm:username:input:username");
    private By emailField = By.id("newUserForm:email:input:email");
    private By saveButton = By.id("newUserForm:newUserSave");
    private By cancelButton = By.linkText("Cancel");
    private String roleIdPrefix = "newUserForm:roles:input:newUserRoles:";

    private Map<String, String> roleMap;

    public CreateUserAccountPage(WebDriver driver) {
        super(driver);
        roleMap = new HashMap<>();
        roleMap.put("admin", "0");
        roleMap.put("glossarist", "1");
        roleMap.put("glossary-admin", "2");
        roleMap.put("translator", "3");
        roleMap.put("user", "4");
    }

    /**
     * Enter text into the username field
     * @param username string to enter
     * @return new CreateUserAccountPage
     */
    public CreateUserAccountPage enterUsername(String username) {
        log.info("Enter username {}", username);
        enterText(usernameField, username);
        return new CreateUserAccountPage(getDriver());
    }

    /**
     * Enter text into the email address field
     * @param email string to enter
     * @return new CreateUserAccountPage
     */
    public CreateUserAccountPage enterEmail(String email) {
        enterText(emailField, email);
        return new CreateUserAccountPage(getDriver());
    }

    /**
     * Select a role for the user
     * @param role entry to select
     * @return new CreateUserAccountPage
     */
    public CreateUserAccountPage clickRole(String role) {
        log.info("Click role {}", role);
        clickElement(By.id(roleIdPrefix.concat(roleMap.get(role))));
        return new CreateUserAccountPage(getDriver());
    }

    /**
     * Query if a role is checked
     * @param role entry to query
     * @return new CreateUserAccountPage
     */
    public boolean isRoleChecked(String role) {
        log.info("Query is role {} checked", role);
        return readyElement(By.id(roleIdPrefix.concat(roleMap.get(role))))
                        .isSelected();
    }

    /**
     * Press the Save button
     * @return new ManageUserPage
     */
    public ManageUserPage saveUser() {
        log.info("Click Save");
        clickElement(saveButton);
        return new ManageUserPage(getDriver());
    }

    /**
     * Press the Save button, expecting a failure condition
     * @return new CreateUserAccountPage
     */
    public CreateUserAccountPage saveUserExpectFailure() {
        log.info("Click Save, expecting failure");
        clickElement(saveButton);
        return new CreateUserAccountPage(getDriver());
    }

    /**
     * Press the Cancel button
     * @return new ManageUserPage
     */
    public ManageUserPage cancelEditUser() {
        log.info("Click Cancel");
        clickElement(cancelButton);
        return new ManageUserPage(getDriver());
    }
}
