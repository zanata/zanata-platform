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
package org.zanata.page.googleaccount;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.AbstractPage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class GoogleAccountPage extends AbstractPage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GoogleAccountPage.class);
    private By emailField = By.id("Email");
    private By emailLabelField = By.id("reauthEmail");
    private By passwordField = By.id("Passwd");
    private By signInButton = By.id("signIn");
    private By signInDifferent =
            By.linkText("Sign in with a different account");
    private By profileImage = By.id("profile-img");

    public GoogleAccountPage(WebDriver driver) {
        super(driver);
    }

    public GoogleAccountPage enterGoogleEmail(String email) {
        log.info("Enter email {}", email);
        enterText(readyElement(emailField), email);
        return new GoogleAccountPage(getDriver());
    }

    public GoogleAccountPage enterGooglePassword(String password) {
        log.info("Enter password {}", password);
        enterText(readyElement(passwordField), password);
        return new GoogleAccountPage(getDriver());
    }

    public GooglePermissionsPage clickSignIn() {
        log.info("Click account Sign In");
        clickElement(signInButton);
        return new GooglePermissionsPage(getDriver());
    }

    public GoogleManagePermissionsPage clickPermissionsSignIn() {
        log.info("Click account management Sign In");
        clickElement(signInButton);
        return new GoogleManagePermissionsPage(getDriver());
    }

    public String rememberedUser() {
        log.info("Query remembered user email");
        return readyElement(emailLabelField).getText();
    }

    public boolean hasRememberedAuthentication() {
        log.info("Query is user remembered");
        return getDriver().findElements(emailLabelField).size() > 0;
    }

    public GoogleAccountPage removeSavedAuthentication() {
        log.info("Click Sign in with different account");
        clickElement(signInDifferent);
        return new GoogleAccountPage(getDriver());
    }

    /**
     * Query if the old google site login is presented to the user.
     *
     * The Google account profile image only shows on the new Google site, so if
     * this element exists return false.
     *
     * @return true if the profile image is not shown.
     */
    public boolean isTheOldGoogleSite() {
        log.info("Query is the old Google site displayed");
        return getDriver().findElements(profileImage).size() < 1;
    }
}
