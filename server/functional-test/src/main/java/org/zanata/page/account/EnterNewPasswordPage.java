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
package org.zanata.page.account;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class EnterNewPasswordPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EnterNewPasswordPage.class);

    public EnterNewPasswordPage(WebDriver driver) {
        super(driver);
    }

    private By newPassworField =
            By.id("passwordResetActivationForm:passwordFieldContainer:input:password");
    private By confirmPassworField =
            By.id("passwordResetActivationForm:confirmPasswordFieldContainer:input:passwordConfirm");
    private By resetPasswordButton =
            By.id("passwordResetActivationForm:resetPasswordButton");

    /**
     * Enter a new password
     * @param password string to enter
     * @return new EnterNewPasswordPage
     */
    public EnterNewPasswordPage enterNewPassword(String password) {
        log.info("Enter new password {}", password);
        enterText(readyElement(newPassworField), password);
        return new EnterNewPasswordPage(getDriver());
    }

    /**
     * Enter a confirmation password
     * @param password string to enter
     * @return new EnterNewPasswordPage
     */
    public EnterNewPasswordPage enterConfirmPassword(String password) {
        log.info("Enter confirm password {}", password);
        enterText(readyElement(confirmPassworField), password);
        return new EnterNewPasswordPage(getDriver());
    }

    /**
     * Press the Change Password button
     * @return new SignInPage
     */
    public SignInPage pressChangePasswordButton() {
        log.info("Press Change Password button");
        clickElement(resetPasswordButton);
        return new SignInPage(getDriver());
    }
}
