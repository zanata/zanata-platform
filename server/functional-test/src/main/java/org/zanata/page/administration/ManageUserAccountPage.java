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
package org.zanata.page.administration;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ManageUserAccountPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ManageUserAccountPage.class);

    public static String PASSWORD_ERROR = "Passwords do not match";
    private By passwordField = By.id("userdetailForm:password:input:password");
    private By passwordConfirmField =
            By.id("userdetailForm:passwordConfirm:input:confirm");
    private By fullNameField = By.id("userdetailForm:name");
    private By enabledField = By.id("userdetailForm:enabled");
    private By saveButton = By.id("userdetailForm:userdetailSave");
    private By cancelButton = By.id("userdetailForm:userdetailCancel");
    private Map<String, String> roleMap;
    private String rolePrefix = "userdetailForm:roles:input:roles:";

    public ManageUserAccountPage(WebDriver driver) {
        super(driver);
        roleMap = new HashMap<>();
        roleMap.put("admin", "0");
        roleMap.put("glossarist", "1");
        roleMap.put("glossary-admin", "2");
        roleMap.put("translator", "3");
        roleMap.put("user", "4");
    }

    /**
     * Enter a display name for the user
     * @param fullName string to enter
     * @return new ManageUserAccountPage
     */
    public ManageUserAccountPage enterFullName(String fullName) {
        log.info("Enter name {}", fullName);
        enterText(fullNameField, fullName);
        return new ManageUserAccountPage(getDriver());
    }

    /**
     * Get the current display name for the user
     * @return display name string
     */
    public String getCurrentName() {
        log.info("Query user's name");
        return getAttribute(fullNameField, "value");
    }

    /**
     * Enter a password for the user
     * @param password string to enter
     * @return new ManageUserAccountPage
     */
    public ManageUserAccountPage enterPassword(String password) {
        log.info("Enter password {}", password);
        enterText(passwordField, password);
        return new ManageUserAccountPage(getDriver());
    }

    /**
     * Enter a confirmation password for the user
     * @param confirmPassword string to enter
     * @return new ManageUserAccountPage
     */
    public ManageUserAccountPage enterConfirmPassword(String confirmPassword) {
        log.info("Enter confirm password {}", confirmPassword);
        enterText(passwordConfirmField, confirmPassword);
        return new ManageUserAccountPage(getDriver());
    }

    /**
     * Press the enabled checkbox
     * @return new ManageUserAccountPage
     */
    public ManageUserAccountPage clickEnabled() {
        log.info("Click Enabled");
        clickElement(enabledField);
        return new ManageUserAccountPage(getDriver());
    }

    /**
     * Press a named role checkbox
     * @param role checkbox name to select
     * @return new ManageUserAccountPage
     */
    public ManageUserAccountPage clickRole(String role) {
        log.info("Click role {}", role);
        clickElement(By.id(rolePrefix.concat(roleMap.get(role))));
        return new ManageUserAccountPage(getDriver());
    }

    /**
     * Query if a named role is checked
     * @param role name to query
     * @return boolean is role checked
     */
    public boolean isRoleChecked(String role) {
        log.info("Query is role {} checked", role);
        return readyElement(By.id(rolePrefix.concat(roleMap.get(role))))
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
     * @return new ManageUserAccountPage
     */
    public ManageUserAccountPage saveUserExpectFailure() {
        log.info("Click Save, expecting failure");
        clickElement(saveButton);
        return new ManageUserAccountPage(getDriver());
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
