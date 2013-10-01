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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class GoogleAccountPage extends AbstractPage {
    @FindBy(id = "Email")
    private WebElement emailField;

    @FindBy(id = "Passwd")
    private WebElement passwordField;

    @FindBy(id = "signIn")
    private WebElement signInButton;

    public GoogleAccountPage(WebDriver driver) {
        super(driver);
    }

    public GoogleAccountPage enterGoogleEmail(String email) {
        emailField.sendKeys(email);
        return new GoogleAccountPage(getDriver());
    }

    public GoogleAccountPage enterGooglePassword(String password) {
        passwordField.sendKeys(password);
        return new GoogleAccountPage(getDriver());
    }

    public GooglePermissionsPage clickSignIn() {
        signInButton.click();
        return new GooglePermissionsPage(getDriver());
    }

    public GoogleManagePermissionsPage clickPermissionsSignIn() {
        signInButton.click();
        return new GoogleManagePermissionsPage(getDriver());
    }
}
