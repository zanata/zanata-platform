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
package org.zanata.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.page.HomePage;
import org.zanata.page.SignInPage;

public class LoginWorkFlow extends AbstractWebWorkFlow
{
   private static final Logger LOGGER = LoggerFactory.getLogger(LoginWorkFlow.class);

   public HomePage signIn(String username, String password)
   {
      // System.getProperties().put("webdriver.firefox.useExisting", "true");
      LOGGER.info("accessing zanata at: {}", hostUrl);

      HomePage homePage = new HomePage(driver);
      if (homePage.hasLoggedIn() && homePage.loggedInAs().equals(username))
      {
         LOGGER.info("already logged in as {}", username);
         return homePage;
      }
      else
      {
         SignInPage signInPage = homePage.clickSignInLink();
         return signInPage.signInAndGoToPage(username, password, HomePage.class);
      }

   }

}
