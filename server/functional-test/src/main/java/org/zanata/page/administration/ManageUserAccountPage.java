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
    private By enabledField = By.id("userdetailForm:enabled");
    private By saveButton = By.id("userdetailForm:userdetailSave");
    private By cancelButton = By.id("userdetailForm:userdetailCancel");
    private Map<String, String> roleMap;

    public ManageUserAccountPage(WebDriver driver) {
        super(driver);
        roleMap = new HashMap<>();
        roleMap.put("admin", "0");
        roleMap.put("glossarist", "1");
        roleMap.put("glossary-admin", "2");
        roleMap.put("translator", "3");
        roleMap.put("user", "4");
    }

    public ManageUserAccountPage enterPassword(String password) {
        log.info("Enter password {}", password);
        enterText(readyElement(passwordField), password);
        return new ManageUserAccountPage(getDriver());
    }

    public ManageUserAccountPage enterConfirmPassword(String confirmPassword) {
        log.info("Enter confirm password {}", confirmPassword);
        enterText(readyElement(passwordConfirmField), confirmPassword);
        return new ManageUserAccountPage(getDriver());
    }

    public ManageUserAccountPage clickEnabled() {
        log.info("Click Enabled");
        clickElement(enabledField);
        return new ManageUserAccountPage(getDriver());
    }

    public ManageUserAccountPage clickRole(String role) {
        log.info("Click role {}", role);
        clickElement(readyElement(By.id("userdetailForm:roles:input:roles:"
                .concat(roleMap.get(role)))));
        return new ManageUserAccountPage(getDriver());
    }

    public boolean isRoleChecked(String role) {
        log.info("Query is role {} checked", role);
        return readyElement(By.id(
                "userdetailForm:rolesField:roles:".concat(roleMap.get(role))))
                        .isSelected();
    }

    public ManageUserPage saveUser() {
        log.info("Click Save");
        clickElement(saveButton);
        return new ManageUserPage(getDriver());
    }

    public ManageUserAccountPage saveUserExpectFailure() {
        log.info("Click Save");
        clickElement(saveButton);
        return new ManageUserAccountPage(getDriver());
    }

    public ManageUserPage cancelEditUser() {
        log.info("Click Cancel");
        clickElement(cancelButton);
        return new ManageUserPage(getDriver());
    }
}
