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
package org.zanata.page.account;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.page.utility.HomePage;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ResetPasswordPage extends BasePage {

    private By usernameField = By.id("passwordResetRequestForm:usernameField:username");
    private By emailField = By.id("passwordResetRequestForm:emailField:email");
    private By submitButton = By.id("passwordResetRequestForm:submitRequest");

    public ResetPasswordPage(WebDriver driver) {
        super(driver);
    }

    public ResetPasswordPage enterUserName(String username) {
        log.info("Enter username {}", username);
        waitForWebElement(usernameField).sendKeys(username);
        return new ResetPasswordPage(getDriver());
    }

    public ResetPasswordPage enterEmail(String email) {
        log.info("Enter email {}", email);
        waitForWebElement(emailField).sendKeys(email);
        return new ResetPasswordPage(getDriver());
    }

    public ResetPasswordPage clearFields() {
        log.info("Clear fields");
        waitForWebElement(usernameField).clear();
        waitForWebElement(emailField).clear();
        return new ResetPasswordPage(getDriver());
    }

    public HomePage resetPassword() {
        log.info("Click Submit");
        defocus(usernameField);
        defocus(emailField);
        waitForWebElement(submitButton).click();
        return new HomePage(getDriver());
    }

    public ResetPasswordPage resetFailure() {
        log.info("Click Submit");
        defocus(usernameField);
        defocus(emailField);
        waitForWebElement(submitButton).click();
        return new ResetPasswordPage(getDriver());
    }
}
