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
package org.zanata.feature.security;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.feature.ConcordionTest;
import org.zanata.page.utility.DashboardPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;


@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
@Category(ConcordionTest.class)
public class LoginTest
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

   public boolean signInAs(String username, String password)
   {
      DashboardPage dashboardPage = new LoginWorkFlow().signIn(username, password);

      return dashboardPage.hasLoggedIn();
   }

   public String getLoggedInUsername()
   {
      HomePage homePage = new LoginWorkFlow().goToHome();
      return homePage == null ? null : homePage.loggedInAs();
   }
   
   public void logout()
   {
      new LoginWorkFlow().goToHome().logout();
   }

}
