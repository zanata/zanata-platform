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
package org.zanata.page.dashboard.dashboardsettings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.dashboard.DashboardBasePage;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DashboardAccountTab extends DashboardBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DashboardAccountTab.class);
    public static final String INCORRECT_OLD_PASSWORD_ERROR =
            "Old password is incorrect, please check and try again.";
    public static final String FIELD_EMPTY_ERROR = "may not be empty";
    public static final String PASSWORD_LENGTH_ERROR =
            "size must be between 6 and 1024";
    public static final String EMAIL_TAKEN_ERROR =
            "This email address is already taken";
    private By emailForm = By.id("email-update-form");
    private By emailField = By.id("email-update-form:emailField:input:email");
    // Use form and button tag to find the item, as its id is altered by jsf
    private By updateEmailButton = By.tagName("button");
    private By oldPasswordField =
            By.id("passwordChangeForm:oldPasswordField:input:oldPassword");
    private By newPasswordField =
            By.id("passwordChangeForm:newPasswordField:input:newPassword");
    private By changePasswordButton = By.cssSelector(
            "button[id^=\'passwordChangeForm:changePasswordButton\']");

    public DashboardAccountTab(WebDriver driver) {
        super(driver);
    }

    public DashboardAccountTab typeNewAccountEmailAddress(String emailAddress) {
        log.info("Enter email {}", emailAddress);
        readyElement(emailField).clear();
        enterText(readyElement(emailField), emailAddress);
        return new DashboardAccountTab(getDriver());
    }

    public DashboardAccountTab clickUpdateEmailButton() {
        log.info("Click Update Email");
        clickElement(readyElement(emailForm).findElement(updateEmailButton));
        return new DashboardAccountTab(getDriver());
    }

    public DashboardAccountTab typeOldPassword(String oldPassword) {
        log.info("Enter old password {}", oldPassword);
        readyElement(oldPasswordField).clear();
        enterText(readyElement(oldPasswordField), oldPassword);
        return new DashboardAccountTab(getDriver());
    }

    public DashboardAccountTab typeNewPassword(String newPassword) {
        log.info("Enter new password {}", newPassword);
        readyElement(newPasswordField).clear();
        enterText(readyElement(newPasswordField), newPassword);
        return new DashboardAccountTab(getDriver());
    }

    public DashboardAccountTab clickUpdatePasswordButton() {
        log.info("Click Update Password");
        clickElement(changePasswordButton);
        return new DashboardAccountTab(getDriver());
    }
}
