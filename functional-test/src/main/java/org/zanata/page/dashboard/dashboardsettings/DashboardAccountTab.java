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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.dashboard.DashboardBasePage;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DashboardAccountTab extends DashboardBasePage {

    public static final String INCORRECT_OLD_PASSWORD_ERROR =
            "Old password is incorrect, please check and try again.";

    public static final String FIELD_EMPTY_ERROR = "may not be empty";

    public static final String PASSWORD_LENGTH_ERROR =
            "size must be between 6 and 20";

    public static final String EMAIL_TAKEN_ERROR =
            "This email address is already taken";

    @FindBy(id = "email-update-form:emailField:email")
    private WebElement emailField;

    @FindBy(id = "email-update-form:updateEmailButton")
    private WebElement updateEmailButton;

    @FindBy(id = "passwordChangeForm:oldPasswordField:oldPassword")
    private WebElement oldPasswordField;

    @FindBy(id = "passwordChangeForm:newPasswordField:newPassword")
    private WebElement newPasswordField;

    @FindBy(id = "passwordChangeForm:changePasswordButton")
    private WebElement changePasswordButton;

    public DashboardAccountTab(WebDriver driver) {
        super(driver);
    }

    public DashboardAccountTab typeNewAccountEmailAddress(String emailAddress) {
        emailField.clear();
        emailField.sendKeys(emailAddress);
        return this;
    }

    public DashboardAccountTab clickUpdateEmailButton() {
        updateEmailButton.click();
        return this;
    }

    public DashboardAccountTab typeOldPassword(String oldPassword) {
        oldPasswordField.clear();
        oldPasswordField.sendKeys(oldPassword);
        return this;
    }

    public DashboardAccountTab typeNewPassword(String newPassword) {
        newPasswordField.clear();
        newPasswordField.sendKeys(newPassword);
        return this;
    }

    public DashboardAccountTab clickUpdatePasswordButton() {
        changePasswordButton.click();
        return this;
    }
}
