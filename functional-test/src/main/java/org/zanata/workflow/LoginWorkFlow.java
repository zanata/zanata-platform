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

import lombok.extern.slf4j.Slf4j;

import org.zanata.page.account.SignInPage;
import org.zanata.page.utility.DashboardPage;

@Slf4j
public class LoginWorkFlow extends AbstractWebWorkFlow
{
   public DashboardPage signIn(String username, String password)
   {
      log.info("accessing zanata at: {}", hostUrl);

      DashboardPage dashboardPage = new DashboardPage(driver);
      if (dashboardPage.hasLoggedIn())
      {
         log.info("already logged in as {}", username);
         if (dashboardPage.loggedInAs().equals(username))
         {
            return dashboardPage;
         }
         log.info("sign out first then sign back in as {}", username);
         dashboardPage.logout();
      }

      SignInPage signInPage = dashboardPage.clickSignInLink().selectInternalSignin();
      return signInPage.signInAndGoToPage(username, password, DashboardPage.class);
   }

}
