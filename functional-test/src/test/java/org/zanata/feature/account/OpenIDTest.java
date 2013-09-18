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
package org.zanata.feature.account;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.BasicAcceptanceTest;
import org.zanata.page.utility.HomePage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.RegisterWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.zanata.util.GoogleSignIn.getSignIn;
import static org.zanata.util.GoogleSignIn.googleIsReachable;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(BasicAcceptanceTest.class)
public class OpenIDTest
{

   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

   @Before
   public void before()
   {
      new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
   }

   @Test
   public void signInWithGoogleOpenID()
   {
      assumeFalse("Environment has Google login data", getSignIn("zanata.test.1").isEmpty());
      assumeTrue("Google can be reached", googleIsReachable());

      String googleUsername = "zanata.test.1";
      String googlePassword = getSignIn(googleUsername);
      HomePage homePage = new RegisterWorkFlow().registerGoogleOpenID(
            "Zanata OpenID",
            "openidtest",
            googlePassword,
            googleUsername.concat("@gmail.com"));
      assertThat("The registration message is shown", homePage.getNotificationMessage(),
            Matchers.equalTo("You will soon receive an email with a link to activate your account."));
   }
}
