/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignInPage extends AbstractPage
{
   private static final Logger LOGGER = LoggerFactory.getLogger(SignInPage.class);

   @FindBy(id = "login:usernameField:username")
   private WebElement usernameField;

   @FindBy(id = "login:passwordField:password")
   private WebElement passwordField;

   @FindBy(id = "login:Sign_in")
   private WebElement signInButton;

   public SignInPage(final WebDriver driver)
   {
      super(driver);
   }

   public <P extends AbstractPage> P signInAndGoToPage(String username, String password, Class<P> pageClass)
   {
      LOGGER.info("log in as username: {}", username);
      usernameField.sendKeys(username);
      passwordField.sendKeys(password);
      signInButton.click();
      return PageFactory.initElements(getDriver(), pageClass);
   }
}
