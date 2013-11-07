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
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class MyAccountPage extends BasePage {

    @FindBy(id = "userConfig:generateApiButton")
    private WebElement generateApiKeyButton;

    @FindBy(id = "apiKeyLabel")
    private WebElement apiKeyLabel;

    @FindBy(id = "configurationTextArea")
    private WebElement configurationTextArea;

    @FindBy(linkText = "Edit Profile")
    private WebElement editProfileButton;

    @FindBy(linkText = "Change Password")
    private WebElement changePasswordButton;

    public MyAccountPage(WebDriver driver) {
        super(driver);
    }

    public ChangePasswordPage goToChangePassword() {
        changePasswordButton.click();
        return new ChangePasswordPage(getDriver());
    }

    public EditProfilePage clickEditProfileButton() {
        editProfileButton.click();
        return new EditProfilePage(getDriver());
    }

    public String getFullName() {
        return getDriver().findElement(By.id("main_body_content"))
                .findElement(By.tagName("h1")).getText();
    }

    public String getUsername() {
        return getDriver().findElement(By.id("main_body_content"))
                .findElement(By.tagName("h3")).getText();
    }

    public MyAccountPage pressApiKeyGenerateButton() {
        generateApiKeyButton.click();
        getDriver().switchTo().alert().accept();
        return new MyAccountPage(getDriver());
    }

    public String getApiKey() {
        return apiKeyLabel.getAttribute("value");
    }

    public String getConfigurationDetails() {
        return configurationTextArea.getText();
    }

}
