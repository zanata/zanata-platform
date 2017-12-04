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


import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.EnterNewPasswordPage;
import org.zanata.page.account.ResetPasswordPage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.EmailQuery;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.EmailQuery.LinkType.PASSWORD_RESET;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class SecurityTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule hasEmailRule = new HasEmailRule();

    @Trace(summary = "The user can log in",
            testCaseIds = 5698)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void signInSuccessful() {
        assertThat(new LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs())
                .isEqualTo("admin")
                .as("User can log in");
    }

    @Trace(summary = "The user must enter a correct username and " +
            "password to log in",
            testCaseIds = 5699)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void signInFailure() {
        assertThat(new LoginWorkFlow()
                .signInFailure("nosuchuser", "password")
                .expectError("Login failed"))
                .contains("Login failed")
                .as("Log in error message is shown");
    }

    @Trace(summary = "The user may reset their password via email",
            testCaseIds = 5700)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void resetPasswordSuccessful() {
        ResetPasswordPage resetPasswordPage = new BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .goToResetPassword()
                .enterUserNameEmail("admin@example.com");
        HomePage homePage = resetPasswordPage.resetPassword();

        assertThat(homePage.getNotificationMessage())
                .isEqualTo("You will soon receive an email with a link to " +
                        "reset your password.");

        WiserMessage message = hasEmailRule.getMessages().get(0);
        String emailContent = HasEmailRule.getEmailContent(message);

        assertThat(message.getEnvelopeReceiver())
                .isEqualTo("admin@example.com")
                .as("Zanata has sent an email to the user");
        assertThat(emailContent)
                .contains("Please follow the link below to reset the " +
                        "password for your account.")
                .as("The system has sent a reset password email to the user");
        assertThat(EmailQuery.hasLink(message, PASSWORD_RESET)).isTrue();

        String resetLink = EmailQuery.getLink(message, PASSWORD_RESET);
        DashboardBasePage dashboardBasePage = new BasicWorkFlow()
                .goToUrl(resetLink, EnterNewPasswordPage.class)
                .enterNewPassword("newpassword")
                .enterConfirmPassword("newpassword")
                .pressChangePasswordButton()
                .enterUsername("admin")
                .enterPassword("newpassword").clickSignIn();

        assertThat(dashboardBasePage.loggedInAs())
                .isEqualTo("admin")
                .as("Admin has signed in with the new password");
    }

    @Trace(summary = "The user must enter a known account or email " +
            "to reset their password")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void resetPasswordFailureForInvalidAccount() {
        ResetPasswordPage resetPasswordPage = new BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .goToResetPassword()
                .enterUserNameEmail("nosuchuser@nosuchdomain.com")
                .resetFailure();

        assertThat(resetPasswordPage.getNotificationMessage(By
                        .id("passwordResetRequestForm:messages")))
                .isEqualTo("No account found.")
                .as("A no such account message is displayed");
    }

    @Trace(summary = "Username or email field must not empty")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emptyResetPasswordFieldEntries() {
        ResetPasswordPage resetPasswordPage = new BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .goToResetPassword()
                .clearUsernameField()
                .resetFailure();

        assertThat(resetPasswordPage.getErrors())
                .contains("value is required")
                .as("value is required error is displayed");
    }

}
