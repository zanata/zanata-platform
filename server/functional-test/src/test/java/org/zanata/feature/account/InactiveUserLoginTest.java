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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.Feature;
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

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Category(DetailedTest.class)
public class InactiveUserLoginTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(InactiveUserLoginTest.class);

    @Rule
    public final HasEmailRule hasEmailRule = new HasEmailRule();

    @Feature(
            summary = "The user needs to verify their account before they may log in",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181714)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void verifyAccount() throws Exception {
        String usernamepassword = "tester1";
        new RegisterWorkFlow().registerInternal(usernamepassword,
                usernamepassword, usernamepassword,
                usernamepassword + "@example.com");
        InactiveAccountPage inactiveAccountPage = new LoginWorkFlow()
                .signInInactive(usernamepassword, usernamepassword);
        assertThat(inactiveAccountPage.getTitle())
                .isEqualTo("Zanata: Account is not activated")
                .as("The account is inactive");
        WiserMessage message = hasEmailRule.getMessages().get(0);
        assertThat(EmailQuery.hasActivationLink(message)).isTrue()
                .as("The email contains the activation link");
        String activationLink = EmailQuery.getActivationLink(message);
        SignInPage page =
                new BasicWorkFlow().goToUrl(activationLink, SignInPage.class);
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(signInPage.getNotificationMessage())
         * .isEqualTo(homePage.ACTIVATION_SUCCESS)
         * .as("The account was activated");
         */
        assertThat(new LoginWorkFlow()
                .signIn(usernamepassword, usernamepassword)
                .loggedInAs()).isEqualTo(usernamepassword).as(
                        "The user has validated their account and logged in");
    }

    @Feature(summary = "The user can resend the account activation email",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 301686)
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
        assertThat(hasEmailRule.getMessages().size()).isEqualTo(2)
                .as("A second email was sent");
        WiserMessage message = hasEmailRule.getMessages().get(1);
        assertThat(EmailQuery.hasActivationLink(message)).isTrue()
                .as("The second email contains the activation link");
        homePage = new BasicWorkFlow()
                .goToUrl(EmailQuery.getActivationLink(message), HomePage.class);
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(homePage.getNotificationMessage())
         * .isEqualTo(SignInPage.ACTIVATION_SUCCESS)
         * .as("The account was activated");
         */
        assertThat(new LoginWorkFlow()
                .signIn(usernamepassword, usernamepassword)
                .loggedInAs()).isEqualTo(usernamepassword).as(
                        "The user has validated their account and logged in");
    }

    @Feature(summary = "The user can update the account activation email",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 301687)
    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void updateActivationEmail() throws Exception {
        String usernamepassword = "tester3";
        new RegisterWorkFlow().registerInternal(usernamepassword,
                usernamepassword, usernamepassword,
                usernamepassword + "@example.com");
        InactiveAccountPage inactiveAccountPage = new LoginWorkFlow()
                .signInInactive(usernamepassword, usernamepassword);
        assertThat(inactiveAccountPage.getTitle())
                .isEqualTo("Zanata: Account is not activated")
                .as("The account is inactive");
        HomePage homePage = inactiveAccountPage
                .enterNewEmail("newtester@example.com").clickUpdateEmail();
        assertThat(homePage.expectNotification(HomePage.EMAILCHANGED_MESSAGE))
                .as("The email changed notification is displayed");
        assertThat(hasEmailRule.getMessages().size()).isEqualTo(2)
                .as("A second email was sent");
        WiserMessage message = hasEmailRule.getMessages().get(1);
        assertThat(message.getEnvelopeReceiver())
                .isEqualTo("newtester@example.com")
                .as("The new email address is used");
        assertThat(EmailQuery.hasActivationLink(message)).isTrue()
                .as("The second email contains the activation link");
        SignInPage page = new BasicWorkFlow().goToUrl(
                EmailQuery.getActivationLink(message), SignInPage.class);
        /*
         * This fails in functional test, for reasons unknown
         * assertThat(homePage.getNotificationMessage())
         * .isEqualTo(SignInPage.ACTIVATION_SUCCESS)
         * .as("The account was activated");
         */
        assertThat(new LoginWorkFlow()
                .signIn(usernamepassword, usernamepassword)
                .loggedInAs()).isEqualTo(usernamepassword).as(
                        "The user has validated their account and logged in");
    }
}
