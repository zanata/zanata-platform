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
package org.zanata.action;

import org.openqa.selenium.WebDriver;
import org.zanata.page.HomePage;
import org.zanata.page.SignInPage;
import org.zanata.page.WebDriverFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LoginAction
{
   private final WebDriver driver;

   public LoginAction()
   {
      driver = WebDriverFactory.INSTANCE.getDriver();
   }

   public HomePage signIn(String homeUrl, String username, String password)
   {
//      System.getProperties().put("webdriver.firefox.useExisting", "true");
      driver.get(homeUrl);

      SignInPage signInPage = new HomePage(driver).clickSignInLink();
      assertThat(signInPage.getTitle(), equalTo("Zanata:Log in"));

      HomePage homePage = signInPage.signInAndGoToPage(username, password, HomePage.class);
      assertThat(homePage.getTitle(), equalTo("Zanata:Home"));
      return homePage;
   }

}
