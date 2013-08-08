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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.BasicAcceptanceTest;
import org.zanata.feature.DetailedTest;
import org.zanata.page.account.ResetPasswordPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;

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
      HomePage homePage = new LoginWorkFlow().signIn("admin", "admin");
      assertThat("User is logged in", homePage.loggedInAs(), Matchers.equalTo("admin"));
   }

   @Test
   public void signInFailure()
   {
      SignInPage signInPage = new BasicWorkFlow().goToHome().clickSignInLink();
      signInPage = signInPage.signInFailure("nosuchuser", "password");
      assertThat("Error message is shown", signInPage.getNotificationMessage(), Matchers.equalTo("Login failed"));
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
            Matchers.equalTo("No such account found"));
   }

   @Test
   public void invalidResetPasswordFieldEntries()
   {
      // Both are valid, but show seemingly at random
      List<String> invalidUsernameErrors = new ArrayList<String>();
      invalidUsernameErrors.add("size must be between 3 and 20");
      invalidUsernameErrors.add("must match ^[a-z\\d_]{3,20}$");

      String invalidEmailError = "not a well-formed email address";
      SignInPage signInPage = new BasicWorkFlow().goToHome().clickSignInLink();
      ResetPasswordPage resetPasswordPage = signInPage.gotToResetPassword();
      resetPasswordPage = resetPasswordPage.enterUserName("b").enterEmail("b");
      resetPasswordPage = resetPasswordPage.resetFailure();

      assertThat("Invalid email error is displayed", resetPasswordPage.waitForErrors(),
            Matchers.hasItem(invalidEmailError));

      assertThat("(One of the) Username error shows",
            invalidUsernameErrors.contains(resetPasswordPage.getErrors().get(0)));

   }

   @Test
   public void emptyResetPasswordFieldEntries()
   {
      // Both are valid, but show seemingly at random
      List<String> emptyUsernameErrors = new ArrayList<String>();
      emptyUsernameErrors.add("size must be between 3 and 20");
      emptyUsernameErrors.add("must match ^[a-z\\d_]{3,20}$");
      String emptyEmailError = "may not be empty";

      SignInPage signInPage = new BasicWorkFlow().goToHome().clickSignInLink();
      ResetPasswordPage resetPasswordPage = signInPage.gotToResetPassword();
      resetPasswordPage = resetPasswordPage.clearFields();
      resetPasswordPage = resetPasswordPage.resetFailure();

      assertThat("Empty email error is displayed", resetPasswordPage.waitForErrors(),
            Matchers.hasItem(emptyEmailError));

      assertThat("(One of the) Username error shows",
            emptyUsernameErrors.contains(resetPasswordPage.getErrors().get(0)));
   }

}
