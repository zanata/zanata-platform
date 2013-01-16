/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.workflow;

import org.zanata.page.HomePage;
import org.zanata.page.SignInPage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginWorkFlow extends AbstractWebWorkFlow
{
   public HomePage signIn(String username, String password)
   {
      // System.getProperties().put("webdriver.firefox.useExisting", "true");
      log.info("accessing zanata at: {}", hostUrl);

      HomePage homePage = new HomePage(driver);
      if (homePage.hasLoggedIn())
      {
         log.info("already logged in as {}", username);
         if (homePage.loggedInAs().equals(username))
         {
            return homePage;
         }
         log.info("sign out first then sign back in as {}", username);
         homePage = homePage.signOut();
      }

      SignInPage signInPage = homePage.clickSignInLink();
      return signInPage.signInAndGoToPage(username, password, HomePage.class);
   }

}
