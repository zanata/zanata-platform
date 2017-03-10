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
import org.zanata.page.BasePage;
import org.zanata.page.utility.HomePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class EditProfilePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EditProfilePage.class);
    private By nameField = By.id("profile-form:nameField:name");
    private By usernameField = By.id("profile-form:usernameField:username");
    private By emailField = By.id("profile-form:emailField:email");
    private By saveButton = By.id("profile-form:user-create-new");
    private By cancelButton = By.id("profile-form:user-create-cancel");

    public EditProfilePage(WebDriver driver) {
        super(driver);
    }

    public EditProfilePage enterName(String name) {
        log.info("Enter name {}", name);
        readyElement(nameField).clear();
        enterText(readyElement(nameField), name);
        defocus(nameField);
        return new EditProfilePage(getDriver());
    }

    public EditProfilePage enterUserName(String userName) {
        log.info("Enter username {}", userName);
        readyElement(usernameField).clear();
        enterText(readyElement(usernameField), userName);
        return new EditProfilePage(getDriver());
    }

    public EditProfilePage enterEmail(String email) {
        log.info("Enter email {}", email);
        readyElement(emailField).clear();
        enterText(readyElement(emailField), email);
        defocus(emailField);
        return new EditProfilePage(getDriver());
    }

    public HomePage clickSave() {
        log.info("Click Save");
        clickElement(saveButton);
        return new HomePage(getDriver());
    }

    public EditProfilePage clickSaveAndExpectErrors() {
        log.info("Click Save");
        clickElement(saveButton);
        return new EditProfilePage(getDriver());
    }
}
