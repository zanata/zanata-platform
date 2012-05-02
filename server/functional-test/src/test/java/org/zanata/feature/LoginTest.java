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
package org.zanata.feature;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.page.HomePage;

import static org.hamcrest.MatcherAssert.assertThat;

public class LoginTest
{
   @Test
   public void canLogIn() {
      LoginWorkFlow loginWorkFlow = new LoginWorkFlow();
      HomePage homePage = loginWorkFlow.signIn("admin", "admin");

      assertThat(homePage.getTitle(), Matchers.equalTo("Zanata:Home"));
      assertThat(homePage.hasLoggedIn(), Matchers.is(true));
      assertThat(homePage.loggedInAs(), Matchers.equalTo("admin"));

      //try to log in again won't cause any problem
      loginWorkFlow.signIn("admin", "admin");
      assertThat(homePage.loggedInAs(), Matchers.equalTo("admin"));
   }

}
