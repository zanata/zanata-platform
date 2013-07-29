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

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ChangePasswordPage extends BasePage
{

   @FindBy(id = "passwordChangeForm:passwordOldField:passwordOld")
   private WebElement oldPasswordField;

   @FindBy(id = "passwordChangeForm:passwordNewField:passwordNew")
   private WebElement newPasswordField;

   @FindBy(id = "passwordChangeForm:passwordConfirmField:passwordConfirm")
   private WebElement confirmPasswordField;

   @FindBy(id = "passwordChangeForm:changePasswordButton")
   private WebElement changePasswordButton;

   @FindBy(id = "passwordChangeForm:cancelChangePasswordButton")
   private WebElement cancelChangePasswordButton;

   public ChangePasswordPage(WebDriver driver)
   {
      super(driver);
   }

   public ChangePasswordPage enterOldPassword(String password)
   {
      oldPasswordField.sendKeys(password);
      return new ChangePasswordPage(getDriver());
   }

   public ChangePasswordPage enterNewPassword(String password)
   {
      newPasswordField.sendKeys(password);
      return new ChangePasswordPage(getDriver());
   }

   public ChangePasswordPage enterConfirmNewPassword(String password)
   {
      confirmPasswordField.sendKeys(password);
      return new ChangePasswordPage(getDriver());
   }

   public MyAccountPage changePassword()
   {
      changePasswordButton.click();
      return new MyAccountPage(getDriver());
   }

   public MyAccountPage cancelChangePassword()
   {
      cancelChangePasswordButton.click();
      return new MyAccountPage(getDriver());
   }

   public ChangePasswordPage changePasswordExpectingFailure()
   {
      changePasswordButton.click();
      return new ChangePasswordPage(getDriver());
   }

}
