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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.CorePage;

import java.util.List;
import java.util.Map;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class RegisterPage extends CorePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RegisterPage.class);

    public static final String USERNAME_VALIDATION_ERROR =
            "Between 3 and 20 lowercase letters, numbers and underscores only";
    public static final String USERNAME_LENGTH_ERROR =
            "size must be between 3 and 20";
    public static final String USERNAME_UNAVAILABLE_ERROR =
            "This username is not available";
    public static final String USERDISPLAYNAME_LENGTH_ERROR =
            "size must be between 2 and 80";
    public static final String MALFORMED_EMAIL_ERROR =
            "not a well-formed email address";
    public static final String REQUIRED_FIELD_ERROR = "may not be empty";
    public static final String EMAIL_TAKEN = "This email address is already taken.";
    public static final String PASSWORD_LENGTH_ERROR =
            "size must be between 6 and 1024";

    private By nameField = By.id("loginForm:name:input:name");
    private By emailField = By.id("loginForm:email:input:email");
    public By usernameField = By.id("loginForm:username:input:username");
    private By passwordField = By.id("loginForm:passwordField:input:password");
    private By signUpButton = By.xpath("//input[@value=\'Sign up\']");
    private By showHideToggleButton = By.className("js-form-password-toggle");
    private By loginLink = By.linkText("Log In");
    private By titleLabel = By.className("heading--sub");
    private By termsOfUseUrl = By.id("termsOfUseUrl");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Enter a display name in the name field
     * @param name string to enter
     * @return new RegisterPage
     */
    public RegisterPage enterName(String name) {
        log.info("Enter name {}", name);
        enterText(readyElement(nameField), name);
        return new RegisterPage(getDriver());
    }

    /**
     * Clear the username field
     * @return new RegisterPage
     */
    public RegisterPage clearUsername() {
        log.info("Clear username field");
        readyElement(usernameField).clear();
        return new RegisterPage(getDriver());
    }

    /**
     * Enter a string in the username field
     * @param userName string to enter
     * @return new RegisterPage
     */
    public RegisterPage enterUserName(String userName) {
        log.info("Enter username {}", userName);
        enterText(readyElement(usernameField), userName);
        return new RegisterPage(getDriver());
    }

    /**
     * Enter a string in the email field
     * @param email string to enter
     * @return new RegisterPage
     */
    public RegisterPage enterEmail(String email) {
        log.info("Enter email {}", email);
        enterText(readyElement(emailField), email);
        return new RegisterPage(getDriver());
    }

    /**
     * Enter a string in the password field
     * @param password string to enter
     * @return new RegisterPage
     */
    public RegisterPage enterPassword(String password) {
        log.info("Enter password {}", password);
        enterText(readyElement(passwordField), password);
        return new RegisterPage(getDriver());
    }

    /**
     * Press the Sign Up button
     * @return new SignInPage
     */
    public SignInPage register() {
        log.info("Click Sign Up");
        clickElement(signUpButton);
        return new SignInPage(getDriver());
    }

    /**
     * Press the Sign Up button, expecting failure
     * @return new SignInPage
     */
    public RegisterPage registerFailure() {
        log.info("Click Sign Up");
        clickElement(signUpButton);
        return new RegisterPage(getDriver());
    }

    /**
     * Clear all registration page fields
     * @return new RegisterPage
     */
    public RegisterPage clearFields() {
        log.info("Clear fields");
        readyElement(nameField).clear();
        readyElement(emailField).clear();
        readyElement(usernameField).clear();
        readyElement(passwordField).clear();
        return new RegisterPage(getDriver());
    }

    /*
     * Pass in a map of strings, to be entered into the registration fields.
     * Fields: name, email, username, password, confirmpassword
     */
    public RegisterPage setFields(Map<String, String> fields) {
        return clearFields().enterName(fields.get("name"))
                .enterEmail(fields.get("email"))
                .enterUserName(fields.get("username"))
                .enterPassword(fields.get("password"));
    }

    /**
     * Retrieve the registration page title
     * @return page title string
     */
    public String getPageTitle() {
        log.info("Query page title");
        return getText(titleLabel);
    }

    /**
     * Press the Log In button
     * @return new SignInPage
     */
    public SignInPage goToSignIn() {
        log.info("Click Log In");
        clickElement(loginLink);
        return new SignInPage(getDriver());
    }

    /**
     * Toggle the show / hide password text
     * @return new RegisterPage
     */
    public RegisterPage clickPasswordShowToggle() {
        log.info("Click Show/Hide");
        clickElement(showHideToggleButton);
        return new RegisterPage(getDriver());
    }

    /**
     * Retrieve the current text of the password field
     * @return password string
     */
    public String getPassword() {
        log.info("Query password");
        return getAttribute(passwordField,"value");
    }

    /**
     * Retrieve the current view type of the password field
     * @return view type string e.g. password, text
     */
    public String getPasswordFieldType() {
        log.info("Query password field type");
        return getAttribute(passwordField,"type");
    }

    /**
     * Determine if the the terms of use url is displayed
     * @return boolean terms of use is visible
     */
    public boolean termsOfUseUrlVisible() {
        log.info("Query terms of use URL is visible");
        List<WebElement> elements = getDriver().findElements(termsOfUseUrl);
        return elements.size() > 0 && elements.get(0).isDisplayed();
    }

    /**
     * Retrieve the URL of the Terms of Use link
     * @return url as string
     */
    public String getTermsUrl() {
        log.info("Query terms of use URL");
        return readyElement(termsOfUseUrl).findElement(By.tagName("a"))
                        .getAttribute("href");
    }
}
