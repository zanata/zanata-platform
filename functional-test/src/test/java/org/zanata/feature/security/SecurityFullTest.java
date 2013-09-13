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
package org.zanata.feature.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.BasicAcceptanceTest;
import org.zanata.feature.DetailedTest;
import org.zanata.page.account.ResetPasswordPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.utility.DashboardPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class SecurityFullTest
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

   @Before
   public void before()
   {
      // Remove all cookies, no previous login is allowed
      new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
   }

   @Test
   @Category(BasicAcceptanceTest.class)
   public void signInSuccessful()
   {
      DashboardPage dashboardPage = new LoginWorkFlow().signIn("admin", "admin");
      assertThat("User is logged in", dashboardPage.loggedInAs(), equalTo("admin"));
   }

   @Test
   public void signInFailure()
   {
      SignInPage signInPage = new LoginWorkFlow().signInFailure("nosuchuser", "password");
      assertThat("Error message is shown", signInPage.getNotificationMessage(), equalTo("Login failed"));
      assertThat("User has failed to log in", !signInPage.hasLoggedIn());
   }

   @Test
   @Ignore("RHBZ-987707 | Cannot intercept email yet")
   public void resetPasswordSuccessful()
   {
      SignInPage signInPage = new BasicWorkFlow().goToHome().clickSignInLink();
      ResetPasswordPage resetPasswordPage = signInPage.gotToResetPassword();
      resetPasswordPage = resetPasswordPage.enterUserName("nosuchuser").enterEmail("nosuchuser@nosuchdomain.com");
      resetPasswordPage = resetPasswordPage.resetPassword();
      //TODO: Reset Success page
   }

   @Test
   public void resetPasswordFailureForInvalidAccount()
   {
      SignInPage signInPage = new BasicWorkFlow().goToHome().clickSignInLink();
      ResetPasswordPage resetPasswordPage = signInPage.gotToResetPassword();
      resetPasswordPage = resetPasswordPage.enterUserName("nosuchuser").enterEmail("nosuchuser@nosuchdomain.com");
      resetPasswordPage = resetPasswordPage.resetFailure();
      assertThat("A no such account message is displayed", resetPasswordPage.getNotificationMessage(),
            equalTo("No such account found"));
   }

   @Test
   public void invalidResetPasswordFieldEntries()
   {
      SignInPage signInPage = new BasicWorkFlow().goToHome().clickSignInLink();
      ResetPasswordPage resetPasswordPage = signInPage.gotToResetPassword();
      resetPasswordPage = resetPasswordPage.enterUserName("b").enterEmail("b");
      resetPasswordPage = resetPasswordPage.resetFailure();

      assertThat("Invalid email error is displayed", resetPasswordPage.waitForErrors(),
            hasItem("not a well-formed email address"));

      // Both are valid, but show seemingly at random
      assertThat(resetPasswordPage.getErrors().get(0),
            either(equalTo("size must be between 3 and 20")).or(equalTo("must match ^[a-z\\d_]{3,20}$")));

   }

   @Test
   public void emptyResetPasswordFieldEntries()
   {
      SignInPage signInPage = new BasicWorkFlow().goToHome().clickSignInLink();
      ResetPasswordPage resetPasswordPage = signInPage.gotToResetPassword();
      resetPasswordPage = resetPasswordPage.clearFields();
      resetPasswordPage = resetPasswordPage.resetFailure();

      assertThat("Empty email error is displayed", resetPasswordPage.waitForErrors(),
            hasItem("may not be empty"));

      // All are valid, but may show at random
      assertThat(resetPasswordPage.getErrors().get(0),
            either(equalTo("size must be between 3 and 20"))
                  .or(equalTo("may not be empty"))
                  .or(equalTo("must match ^[a-z\\d_]{3,20}$")));

   }

}
