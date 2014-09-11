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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */

@Slf4j
public class ManageUserAccountPage extends BasePage {

    public static String PASSWORD_ERROR = "Passwords do not match";

    @FindBy(id = "userdetailForm:passwordField:password")
    private WebElement passwordField;

    @FindBy(id = "userdetailForm:passwordConfirmField:confirm")
    private WebElement passwordConfirmField;

    @FindBy(id = "userdetailForm:enabledField:enabled")
    private WebElement enabledField;

    @FindBy(id = "userdetailForm:userdetailSave")
    private WebElement saveButton;

    @FindBy(id = "userdetailForm:userdetailCancel")
    private WebElement cancelButton;

    // username field will trigger ajax call and become stale
    private By usernameBy = By.id("userdetailForm:usernameField:username");

    private Map<String, String> roleMap;

    public ManageUserAccountPage(WebDriver driver) {
        super(driver);
        roleMap = new HashMap<String, String>();
        roleMap.put("admin", "0");
        roleMap.put("glossarist", "1");
        roleMap.put("glossary-admin", "2");
        roleMap.put("translator", "3");
        roleMap.put("user", "4");
    }

    public ManageUserAccountPage enterPassword(String password) {
        log.info("Enter password {}", password);
        passwordField.sendKeys(password);
        return new ManageUserAccountPage(getDriver());
    }

    public ManageUserAccountPage enterConfirmPassword(String confirmPassword) {
        log.info("Enter confirm password {}", confirmPassword);
        passwordConfirmField.sendKeys(confirmPassword);
        return new ManageUserAccountPage(getDriver());
    }

    public ManageUserAccountPage clickEnabled() {
        log.info("Click Enabled");
        enabledField.click();
        return new ManageUserAccountPage(getDriver());
    }

    public ManageUserAccountPage clickRole(String role) {
        log.info("Click role {}", role);
        WebElement roleBox =
                getDriver().findElement(
                        By.id("userdetailForm:rolesField:roles:".concat(roleMap
                                .get(role))));
        roleBox.click();
        return new ManageUserAccountPage(getDriver());
    }

    public boolean isRoleChecked(String role) {
        log.info("Query is role {} checked", role);
        return getDriver().findElement(
                By.id("userdetailForm:rolesField:roles:".concat(roleMap
                        .get(role)))).isSelected();
    }

    public ManageUserPage saveUser() {
        log.info("Click Save");
        saveButton.click();
        return new ManageUserPage(getDriver());
    }

    public ManageUserAccountPage saveUserExpectFailure() {
        log.info("Click Save");
        saveButton.click();
        return new ManageUserAccountPage(getDriver());
    }

    public ManageUserPage cancelEditUser() {
        log.info("Click Cancel");
        cancelButton.click();
        return new ManageUserPage(getDriver());
    }

}
