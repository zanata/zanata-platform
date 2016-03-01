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
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.ResetPasswordPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class SecurityTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule hasEmailRule = new HasEmailRule();

    @Feature(summary = "The user can log in",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86815)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void signInSuccessful() {
        assertThat(new LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs())
                .isEqualTo("admin")
                .as("User can log in");
    }

    @Feature(summary = "The user must enter a correct username and " +
            "password to log in",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86815)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void signInFailure() {
        assertThat(new LoginWorkFlow()
                .signInFailure("nosuchuser", "password")
                .expectError("Login failed"))
                .contains("Login failed")
                .as("Log in error message is shown");
    }

    @Feature(summary = "The user may reset their password via email",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
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
    }

    @Feature(summary = "The user must enter a known account or email " +
            "to reset their password",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
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

    @Feature(summary = "Username or email field must not empty",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emptyResetPasswordFieldEntries() {
        ResetPasswordPage resetPasswordPage = new BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .goToResetPassword()
                .clearFields()
                .resetFailure();

        assertThat(resetPasswordPage.getErrors())
                .contains("value is required")
                .as("value is required error is displayed");
    }

}
