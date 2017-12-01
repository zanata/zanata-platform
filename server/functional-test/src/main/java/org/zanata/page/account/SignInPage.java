/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
import org.zanata.page.CorePage;
import org.zanata.page.dashboard.DashboardBasePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class SignInPage extends CorePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SignInPage.class);

    public static final String LOGIN_FAILED_ERROR = "Login failed";
    public static final String ACTIVATION_SUCCESS =
            "Your account was successfully activated. You can now sign in.";
    private By usernameField = By.id("loginForm:username");
    private By passwordField = By.id("loginForm:password");
    private By signInButton = By.id("loginForm:loginButton");
    private By forgotPasswordLink = By.linkText("Forgot your password?");
    private By signUpLink = By.linkText("Sign Up");
    private By titleLabel = By.className("heading--sub");

    public SignInPage(final WebDriver driver) {
        super(driver);
    }

    /**
     * Enter a string into the username field
     * @param username string to enter
     * @return new SignInPage
     */
    public SignInPage enterUsername(String username) {
        log.info("Enter username {}", username);
        enterText(readyElement(usernameField), username);
        return new SignInPage(getDriver());
    }

    /**
     * Enter a string into the password field
     * @param password string to enter
     * @return new SignInPage
     */
    public SignInPage enterPassword(String password) {
        log.info("Enter password {}", password);
        enterText(readyElement(passwordField), password);
        return new SignInPage(getDriver());
    }

    /**
     * Press the sign in button
     * @return new DashboardBasePage
     */
    public DashboardBasePage clickSignIn() {
        log.info("Click Sign In");
        clickElement(signInButton);
        return new DashboardBasePage(getDriver());
    }

    /**
     * Press the sign in button, expecting failure
     * @return new SignInPage
     */
    public SignInPage clickSignInExpectError() {
        log.info("Click Sign In");
        clickElement(signInButton);
        return new SignInPage(getDriver());
    }

    /**
     * Press the sign in button, expecting an 'account inactive' response
     * @return new InactiveAccountPage
     */
    public InactiveAccountPage clickSignInExpectInactive() {
        log.info("Click Sign In");
        clickElement(signInButton);
        return new InactiveAccountPage(getDriver());
    }

    /**
     * Press the Forgot Password link
     * @return new ResetPasswordPage
     */
    public ResetPasswordPage goToResetPassword() {
        log.info("Click Forgot Password");
        clickElement(forgotPasswordLink);
        return new ResetPasswordPage(getDriver());
    }

    /**
     * Press the Sign Up button
     * @return new RegisterPage
     */
    public RegisterPage goToRegister() {
        log.info("Click Sign Up");
        clickElement(signUpLink);
        return new RegisterPage(getDriver());
    }

    /**
     * Retrieve the login page title
     * @return page title string
     */
    public String getPageTitle() {
        log.info("Query page title");
        return readyElement(titleLabel).getText();
    }
}
