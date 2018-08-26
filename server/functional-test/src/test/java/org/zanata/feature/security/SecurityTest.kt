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
package org.zanata.feature.security


import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.EnterNewPasswordPage
import org.zanata.util.EmailQuery
import org.zanata.util.HasEmailExtension
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.zanata.util.EmailQuery.LinkType.PASSWORD_RESET

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
@ExtendWith(HasEmailExtension::class)
class SecurityTest : ZanataTestCase() {

    @Trace(summary = "The user can log in", testCaseIds = [5698])
    @Test
    fun signInSuccessful() {
        assertThat(LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs())
                .describedAs("User can log in")
                .isEqualTo("admin")
    }

    @Trace(summary = "The user must enter a correct username and " + "password to log in", testCaseIds = [5699])
    @Test
    fun signInFailure() {
        assertThat(LoginWorkFlow()
                .signInFailure("nosuchuser", "password")
                .expectError("Login failed"))
                .describedAs("Log in error message is shown")
                .contains("Login failed")
    }

    @Trace(summary = "The user may reset their password via email", testCaseIds = [5700])
    @Test
    fun resetPasswordSuccessful() {
        val resetPasswordPage = BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .goToResetPassword()
                .enterUserNameEmail("admin@example.com")
        val homePage = resetPasswordPage.resetPassword()

        assertThat(homePage.notificationMessage)
                .isEqualTo("You will soon receive an email with a link to " + "reset your password.")

        val messages = hasEmailExtension.messages
        assertThat(messages).hasSize(1)
        val message = messages[0]
        val emailContent = HasEmailExtension.getEmailContent(message)

        assertThat(message.envelopeReceiver)
                .describedAs("Zanata has sent an email to the user")
                .isEqualTo("admin@example.com")
        assertThat(emailContent)
                .describedAs("The system has sent a reset password email to the user")
                .contains("Please follow the link below to reset the " + "password for your account.")
        assertThat(EmailQuery.hasLink(message, PASSWORD_RESET)).isTrue()

        val resetLink = EmailQuery.getLink(message, PASSWORD_RESET)
        val dashboardBasePage = BasicWorkFlow()
                .goToUrl(resetLink, EnterNewPasswordPage::class.java)
                .enterNewPassword("newpassword")
                .enterConfirmPassword("newpassword")
                .pressChangePasswordButton()
                .enterUsername("admin")
                .enterPassword("newpassword").clickSignIn()

        assertThat(dashboardBasePage.loggedInAs())
                .describedAs("Admin has signed in with the new password")
                .isEqualTo("admin")
    }

    @Trace(summary = "The user must enter a known account or email " + "to reset their password")
    @Test
    fun resetPasswordFailureForInvalidAccount() {
        val resetPasswordPage = BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .goToResetPassword()
                .enterUserNameEmail("nosuchuser@nosuchdomain.com")
                .resetFailure()

        assertThat(resetPasswordPage.getNotificationMessage(By
                .id("passwordResetRequestForm:messages")))
                .describedAs("A no such account message is displayed")
                .isEqualTo("No account found.")
    }

    @Trace(summary = "Username or email field must not empty")
    @Test
    fun emptyResetPasswordFieldEntries() {
        val resetPasswordPage = BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .goToResetPassword()
                .clearUsernameField()
                .resetFailure()

        assertThat(resetPasswordPage.errors)
                .describedAs("value is required error is displayed")
                .contains("value is required")
    }

}
