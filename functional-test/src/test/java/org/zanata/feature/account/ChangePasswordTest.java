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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.BasicAcceptanceTest;
import org.zanata.feature.DetailedTest;
import org.zanata.page.account.ChangePasswordPage;
import org.zanata.page.account.MyAccountPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ChangePasswordTest
{
   @Rule
   public ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

   @Before
   public void setUp()
   {
      new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
   }

   @Test
   @Category(BasicAcceptanceTest.class)
   public void changePasswordSuccessful()
   {
      MyAccountPage myAccountPage = new LoginWorkFlow().signIn("translator", "translator")
            .goToMyProfile()
            .goToChangePassword()
            .enterOldPassword("translator")
            .enterNewPassword("newpassword")
            .enterConfirmNewPassword("newpassword")
            .changePassword();

      assertThat("Confirmation message is displayed", myAccountPage.getNotificationMessage(),
            Matchers.equalTo("Your password has been successfully changed."));

      HomePage homePage = myAccountPage.signOut();
      assertThat("User is logged out", !homePage.hasLoggedIn());
      homePage = new LoginWorkFlow().signIn("translator", "newpassword");
      assertThat("User has logged in with the new password", homePage.hasLoggedIn());
   }

   @Test
   public void changePasswordCurrentPasswordFailure()
   {
      String incorrectPassword = "Old password is incorrect, please check and try again.";
      ChangePasswordPage changePasswordPage = new LoginWorkFlow().signIn("translator", "translator")
            .goToMyProfile()
            .goToChangePassword()
            .enterOldPassword("nottherightpassword")
            .enterNewPassword("somenewpassword")
            .enterConfirmNewPassword("somenewpassword")
            .changePasswordExpectingFailure();

      assertThat("Incorrect password message displayed", changePasswordPage.getErrors(),
            Matchers.contains(incorrectPassword));
   }

   @Test
   public void changePasswordConfirmationMismatch()
   {
      String incorrectPassword = "Passwords do not match";
      ChangePasswordPage changePasswordPage = new LoginWorkFlow().signIn("translator", "translator")
            .goToMyProfile()
            .goToChangePassword()
            .enterOldPassword("translator")
            .enterNewPassword("somenewpassword")
            .enterConfirmNewPassword("differentpassword")
            .changePasswordExpectingFailure();

      assertThat("Incorrect password message displayed", changePasswordPage.getErrors(),
            Matchers.contains(incorrectPassword));
   }

   @Test
   public void changePasswordCancel()
   {
      MyAccountPage myAccountPage = new LoginWorkFlow().signIn("translator", "translator")
            .goToMyProfile()
            .goToChangePassword()
            .enterOldPassword("translator")
            .enterNewPassword("notnewpassword")
            .enterConfirmNewPassword("notnewpassword")
            .cancelChangePassword();

      HomePage homePage = myAccountPage.signOut();
      assertThat("User is logged out", !homePage.hasLoggedIn());
      homePage = new LoginWorkFlow().signIn("translator", "translator");
      assertThat("User has logged in with the original password", homePage.hasLoggedIn());
   }

   @Test
   public void changePasswordRequiredFieldsAreNotEmpty()
   {
      String emptyPassword = "value is required";
      ChangePasswordPage changePasswordPage = new LoginWorkFlow().signIn("translator", "translator")
            .goToMyProfile()
            .goToChangePassword()
            .changePasswordExpectingFailure();

      assertThat("Incorrect password message displayed", changePasswordPage.getErrors(),
            Matchers.contains(emptyPassword, emptyPassword, emptyPassword));
   }

   @Test
   public void changePasswordAreOfRequiredLength()
   {
      String passwordSizeError = "size must be between 6 and 20";
      String tooShort = "test5";
      String tooLong = "t12345678901234567890";
      ChangePasswordPage changePasswordPage = new LoginWorkFlow().signIn("translator", "translator")
            .goToMyProfile()
            .goToChangePassword()
            .enterNewPassword("test5")
            .enterConfirmNewPassword(tooShort);

      assertThat("Incorrect password message displayed", changePasswordPage.waitForErrors(),
            Matchers.hasItem(passwordSizeError));

      changePasswordPage = changePasswordPage.enterNewPassword(tooLong).enterConfirmNewPassword(tooLong);

      assertThat("Incorrect password message displayed", changePasswordPage.waitForErrors(),
            Matchers.hasItem(passwordSizeError));
   }
}
