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
package org.zanata.page.administration;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */

public class ManageUserAccountPage extends AbstractPage
{

   @FindBy(id = "userdetailForm:usernameField:username")
   private WebElement usernameField;

   @FindBy(id = "userdetailForm:passwordField:password")
   private WebElement passwordField;

   @FindBy(id = "userdetailForm:passwordConfirmField:confirm")
   private WebElement passwordConfirmField;

   @FindBy(id = "userdetailForm:enabledField:enabled")
   private WebElement enabledField;

   @FindBy(id = "userdetailForm:userdetailSave")
   private WebElement saveButton;

   @FindBy(id = "userdetailForm:userdetailCancel")
   private WebElement cancelButton;

   private Map<String, String> roleMap;

   public ManageUserAccountPage(WebDriver driver)
   {
      super(driver);
      roleMap = new HashMap();
      roleMap.put("admin", "0");
      roleMap.put("glossarist", "1");
      roleMap.put("glossary-admin", "2");
      roleMap.put("translator", "3");
      roleMap.put("user", "4");
   }

   public ManageUserAccountPage enterUsername(String username)
   {
      usernameField.sendKeys(username);
      return new ManageUserAccountPage(getDriver());
   }

   public ManageUserAccountPage enterPassword(String password)
   {
      passwordField.sendKeys(password);
      return new ManageUserAccountPage(getDriver());
   }

   public ManageUserAccountPage enterConfirmPassword(String confirmPassword)
   {
      passwordConfirmField.sendKeys(confirmPassword);
      return new ManageUserAccountPage(getDriver());
   }

   public ManageUserAccountPage clickEnabled()
   {
      enabledField.click();
      return new ManageUserAccountPage(getDriver());
   }

   public ManageUserAccountPage clickRole(String role)
   {
      WebElement roleBox = getDriver().findElement(By.id("userdetailForm:rolesField:roles:".concat(roleMap.get(role))));
      roleBox.click();
      return new ManageUserAccountPage(getDriver());
   }

   public boolean isRoleChecked(String role)
   {
      return getDriver().findElement(By.id("userdetailForm:rolesField:roles:".concat(roleMap.get(role)))).isSelected();
   }

   public ManageUserPage saveUser()
   {
      saveButton.click();
      return new ManageUserPage(getDriver());
   }

   public ManageUserPage cancelEditUser()
   {
      cancelButton.click();
      return new ManageUserPage(getDriver());
   }

   public ManageUserAccountPage clearFields()
   {
      usernameField.clear();
      passwordField.clear();
      passwordConfirmField.clear();
      return new ManageUserAccountPage(getDriver());
   }
}
