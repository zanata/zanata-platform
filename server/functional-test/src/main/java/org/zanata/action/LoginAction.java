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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.page.HomePage;
import org.zanata.page.SignInPage;
import org.zanata.page.WebDriverFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LoginAction
{
   private static final Logger LOGGER = LoggerFactory.getLogger(LoginAction.class);
   private final WebDriver driver;
   private final String hostUrl;

   public LoginAction()
   {
      driver = WebDriverFactory.INSTANCE.getDriver();
      hostUrl = WebDriverFactory.INSTANCE.getHostUrl();
   }

   public HomePage signIn(String username, String password)
   {
//      System.getProperties().put("webdriver.firefox.useExisting", "true");
      LOGGER.info("accessing zanata at: {}", hostUrl);
      driver.get(hostUrl);

      SignInPage signInPage = new HomePage(driver).clickSignInLink();

      return signInPage.signInAndGoToPage(username, password, HomePage.class);
   }

}
