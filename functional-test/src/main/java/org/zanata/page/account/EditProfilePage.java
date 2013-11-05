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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.utility.HomePage;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class EditProfilePage extends BasePage {
    @FindBy(id = "editProfileForm:nameField:name")
    private WebElement nameField;

    @FindBy(id = "editProfileForm:usernameField:username")
    private WebElement usernameField;

    @FindBy(id = "editProfileForm:emailField:email")
    private WebElement emailField;

    @FindBy(id = "editProfileForm:saveButton")
    private WebElement saveButton;

    @FindBy(id = "editProfileForm:cancelButton")
    private WebElement cancelButton;

    public EditProfilePage(WebDriver driver) {
        super(driver);
    }

    public EditProfilePage enterName(String name) {
        nameField.clear();
        nameField.sendKeys(name);
        return new EditProfilePage(getDriver());
    }

    public EditProfilePage enterUserName(String userName) {
        usernameField.sendKeys(userName);
        return new EditProfilePage(getDriver());
    }

    public EditProfilePage enterEmail(String email) {
        emailField.clear();
        emailField.sendKeys(email);
        return new EditProfilePage(getDriver());
    }

    public HomePage clickSave() {
        saveButton.click();
        return new HomePage(getDriver());
    }

    public MyAccountPage clickSaveChanges() {
        saveButton.click();
        return new MyAccountPage(getDriver());
    }

    public EditProfilePage clickSaveAndExpectErrors() {
        saveButton.click();
        return new EditProfilePage(getDriver());
    }

    public MyAccountPage clickCancel() {
        cancelButton.click();
        return new MyAccountPage(getDriver());
    }
}
