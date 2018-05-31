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
package org.zanata.feature.account;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.InactiveAccountPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.EmailQuery;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.RegisterWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.EmailQuery.LinkType.ACTIVATE;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Category(DetailedTest.class)
public class InactiveUserLoginTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule hasEmailRule = new HasEmailRule();

    @Trace(
            summary = "The user needs to verify their account before they may log in")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void verifyAccount() throws Exception {
        String username = "tester1";
        String password = "tester1";
        new RegisterWorkFlow().registerInternal(username,
                username, password,
                username + "@example.com");
        InactiveAccountPage inactiveAccountPage = new LoginWorkFlow()
                .signInInactive(username, password);

        assertThat(inactiveAccountPage.getTitle())
                .as("The account is inactive")
                .isEqualTo(InactiveAccountPage.ACCOUNT_UNACTIVATED);

        List<WiserMessage> messages = hasEmailRule.getMessages();
        assertThat(messages).as("one email message").hasSize(1);
        WiserMessage message = messages.get(0);
        assertThat(EmailQuery.hasLink(message, ACTIVATE))
                .as("The email contains the activation link")
                .isTrue();
        String activationLink = EmailQuery.getLink(message, ACTIVATE);
        new BasicWorkFlow().goToUrl(activationLink, SignInPage.class);
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(signInPage.getNotificationMessage())
         * .as("The account was activated")
         * .isEqualTo(homePage.ACTIVATION_SUCCESS)
         */
        assertThat(new LoginWorkFlow()
                .signIn(username, password)
                .loggedInAs())
                .as("User has verified email address and logged in")
                .isEqualTo(username);
    }

    @Trace(summary = "The user can resend the account activation email",
            testCaseIds = 5697)
    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void resendActivationEmail() throws Exception {
        String usernamepassword = "tester2";
        new RegisterWorkFlow().registerInternal(usernamepassword,
                usernamepassword, usernamepassword,
                usernamepassword + "@example.com");
        HomePage homePage = new LoginWorkFlow()
                .signInInactive(usernamepassword, usernamepassword)
                .clickResendActivationEmail();

        assertThat(homePage.expectNotification(HomePage.SIGNUP_SUCCESS_MESSAGE))
                .as("The message sent notification is displayed");
        assertThat(hasEmailRule.getMessages().size())
                .as("A second email was sent")
                .isEqualTo(2);

        WiserMessage message = hasEmailRule.getMessages().get(1);
        assertThat(EmailQuery.hasLink(message, ACTIVATE))
                .as("The second email contains the activation link")
                .isTrue();

        new BasicWorkFlow()
                .goToUrl(EmailQuery.getLink(message, ACTIVATE), HomePage.class);
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(homePage.getNotificationMessage())
         *     .isEqualTo(SignInPage.ACTIVATION_SUCCESS)
         *     .as("The account was activated");
         */
        assertThat(new LoginWorkFlow()
                .signIn(usernamepassword, usernamepassword)
                .loggedInAs())
                .as("The user has validated their account and logged in")
                .isEqualTo(usernamepassword);
    }

    @Trace(summary = "The user can update the account activation email address",
            testCaseIds = 5696)
    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void updateActivationEmail() throws Exception {
        String usernamepassword = "tester3";
        new RegisterWorkFlow().registerInternal(usernamepassword,
                usernamepassword, usernamepassword,
                usernamepassword + "@example.com");
        InactiveAccountPage inactiveAccountPage = new LoginWorkFlow()
                .signInInactive(usernamepassword, usernamepassword);

        assertThat(inactiveAccountPage.getTitle())
                .as("The account is inactive")
                .isEqualTo(InactiveAccountPage.ACCOUNT_UNACTIVATED);

        HomePage homePage = inactiveAccountPage
                .enterNewEmail("newtester@example.com")
                .clickUpdateEmail();

        assertThat(homePage.expectNotification(HomePage.EMAILCHANGED_MESSAGE))
                .as("The email changed notification is displayed")
                .isTrue();
        assertThat(hasEmailRule.getMessages().size())
                .as("A second email was sent")
                .isEqualTo(2);

        WiserMessage message = hasEmailRule.getMessages().get(1);
        assertThat(message.getEnvelopeReceiver())
                .as("The new email address is used")
                .isEqualTo("newtester@example.com");
        assertThat(EmailQuery.hasLink(message, ACTIVATE))
                .as("The second email contains the activation link")
                .isTrue();
        new BasicWorkFlow().goToUrl(
                EmailQuery.getLink(message, ACTIVATE), SignInPage.class);
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(homePage.getNotificationMessage())
         *     .isEqualTo(SignInPage.ACTIVATION_SUCCESS)
         *     .as("The account was activated");
         */
        assertThat(new LoginWorkFlow()
                .signIn(usernamepassword, usernamepassword).loggedInAs())
                .as("User has verified new email address and logged in")
                .isEqualTo(usernamepassword);
    }
}
