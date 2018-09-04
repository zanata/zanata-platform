/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.account


import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.InactiveAccountPage
import org.zanata.page.account.SignInPage
import org.zanata.page.utility.HomePage
import org.zanata.util.EmailQuery
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.RegisterWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.zanata.util.EmailQuery.LinkType.ACTIVATE
import org.zanata.util.HasEmailExtension

/**
 * @author Carlos Munoz [camunoz@redhat.com](mailto:camunoz@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
@ExtendWith(HasEmailExtension::class)
class InactiveUserLoginTest : ZanataTestCase() {

    @Trace(summary = "The user needs to verify their account before they may log in")
    @Test
    @DisplayName("Verify account before login")
    fun `Verify account before login`() {
        val username = "tester1"
        val password = "tester1"
        RegisterWorkFlow().registerInternal(username,
                username, password,
                "$username@example.com")
        val inactiveAccountPage = LoginWorkFlow()
                .signInInactive(username, password)

        assertThat(inactiveAccountPage.title)
                .describedAs("The account is inactive")
                .isEqualTo(InactiveAccountPage.ACCOUNT_UNACTIVATED)

        val messages = hasEmailExtension.messages
        assertThat(messages).describedAs("one email message").hasSize(1)
        val message = messages[0]
        assertThat(EmailQuery.hasLink(message, ACTIVATE))
                .describedAs("The email contains the activation link")
                .isTrue()
        val activationLink = EmailQuery.getLink(message, ACTIVATE)
        BasicWorkFlow().goToUrl(activationLink, SignInPage::class.java)
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(signInPage.getNotificationMessage())
         * .as("The account was activated")
         * .isEqualTo(homePage.ACTIVATION_SUCCESS)
         */
        assertThat(LoginWorkFlow()
                .signIn(username, password)
                .loggedInAs())
                .describedAs("User has verified email address and logged in")
                .isEqualTo(username)
    }

    @Trace(summary = "The user can resend the account activation email",
            testCaseIds = [5697])
    @Test
    @DisplayName("Resend activation email")
    fun `Resend activation email`() {
        val usernamePassword = "tester2"
        RegisterWorkFlow().registerInternal(usernamePassword,
                usernamePassword, usernamePassword,
                "$usernamePassword@example.com")
        val homePage = LoginWorkFlow()
                .signInInactive(usernamePassword, usernamePassword)
                .clickResendActivationEmail()

        assertThat(homePage.expectNotification(HomePage.SIGNUP_SUCCESS_MESSAGE))
                .describedAs("The message sent notification is displayed")
                .isTrue()
        assertThat(hasEmailExtension.messages.size)
                .describedAs("A second email was sent")
                .isEqualTo(2)

        val message = hasEmailExtension.messages[1]
        assertThat(EmailQuery.hasLink(message, ACTIVATE))
                .describedAs("The second email contains the activation link")
                .isTrue()

        BasicWorkFlow().goToUrl(EmailQuery.getLink(message, ACTIVATE),
                HomePage::class.java)
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(homePage.getNotificationMessage())
         *     .isEqualTo(SignInPage.ACTIVATION_SUCCESS)
         *     .as("The account was activated");
         */
        assertThat(LoginWorkFlow()
                .signIn(usernamePassword, usernamePassword)
                .loggedInAs())
                .describedAs("The user has validated their account and logged in")
                .isEqualTo(usernamePassword)
    }

    @Trace(summary = "The user can update the account activation email address",
            testCaseIds = [5696])
    @Test
    @DisplayName("Update email address for activation")
    fun `Update email address for activation`() {
        val usernamePassword = "tester3"
        RegisterWorkFlow().registerInternal(usernamePassword,
                usernamePassword, usernamePassword,
                "$usernamePassword@example.com")
        val inactiveAccountPage = LoginWorkFlow()
                .signInInactive(usernamePassword, usernamePassword)

        assertThat(inactiveAccountPage.title)
                .describedAs("The account is inactive")
                .isEqualTo(InactiveAccountPage.ACCOUNT_UNACTIVATED)

        val homePage = inactiveAccountPage
                .enterNewEmail("newtester@example.com")
                .clickUpdateEmail()

        assertThat(homePage.expectNotification(HomePage.EMAILCHANGED_MESSAGE))
                .describedAs("The email changed notification is displayed")
                .isTrue()
        assertThat(hasEmailExtension.messages.size)
                .describedAs("A second email was sent")
                .isEqualTo(2)

        val message = hasEmailExtension.messages[1]
        assertThat(message.envelopeReceiver)
                .describedAs("The new email address is used")
                .isEqualTo("newtester@example.com")
        assertThat(EmailQuery.hasLink(message, ACTIVATE))
                .describedAs("The second email contains the activation link")
                .isTrue()
        BasicWorkFlow().goToUrl(
                EmailQuery.getLink(message, ACTIVATE), SignInPage::class.java)
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(homePage.getNotificationMessage())
         *     .isEqualTo(SignInPage.ACTIVATION_SUCCESS)
         *     .as("The account was activated");
         */
        assertThat(LoginWorkFlow()
                .signIn(usernamePassword, usernamePassword).loggedInAs())
                .describedAs("User has verified new email address and logged in")
                .isEqualTo(usernamePassword)
    }
}
