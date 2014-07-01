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

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.account.ResetPasswordPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.util.AddUsersRule;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class SecurityFullTest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();
    @ClassRule
    public static HasEmailRule emailRule = new HasEmailRule();

    @Before
    public void before() {
        // Remove all cookies, no previous login is allowed
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void signInSuccessful() {
        DashboardBasePage dashboardPage =
                new LoginWorkFlow().signIn("admin", "admin");
        assertThat("User is logged in", dashboardPage.loggedInAs(),
                equalTo("admin"));
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void signInFailure() {
        SignInPage signInPage = new LoginWorkFlow()
                .signInFailure("nosuchuser", "password");

        assertThat("Error message is shown",
                signInPage.waitForFieldErrors(),
                Matchers.hasItem("Login failed"));
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void resetPasswordSuccessful() {
        SignInPage signInPage =
                new BasicWorkFlow().goToHome().clickSignInLink();
        ResetPasswordPage resetPasswordPage = signInPage.goToResetPassword();
        resetPasswordPage =
                resetPasswordPage.enterUserName("translator").enterEmail(
                        "translator@example.com");
        resetPasswordPage = resetPasswordPage.resetPassword();
        WiserMessage wiserMessage = emailRule.getMessages().get(0);
        String emailContent = new String(wiserMessage.getData());
        Assertions.assertThat(
                resetPasswordPage.getNotificationMessage()).contains(
                "You will soon receive an email with a link to reset your password.1");
        Assertions
                .assertThat(emailContent)
                .contains(
                        "Please follow the link below to reset the password for your account.");
        // TODO: Reset Success page
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void resetPasswordFailureForInvalidAccount() {
        SignInPage signInPage =
                new BasicWorkFlow().goToHome().clickSignInLink();
        ResetPasswordPage resetPasswordPage = signInPage.goToResetPassword();
        resetPasswordPage =
                resetPasswordPage.enterUserName("nosuchuser").enterEmail(
                        "nosuchuser@nosuchdomain.com");
        resetPasswordPage = resetPasswordPage.resetFailure();
        assertThat("A no such account message is displayed",
                resetPasswordPage.getNotificationMessage(),
                equalTo("No such account found"));
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void invalidResetPasswordFieldEntries() {
        SignInPage signInPage =
                new BasicWorkFlow().goToHome().clickSignInLink();
        ResetPasswordPage resetPasswordPage = signInPage.goToResetPassword();
        resetPasswordPage =
                resetPasswordPage.enterUserName("b").enterEmail("b");
        resetPasswordPage = resetPasswordPage.resetFailure();

        assertThat("Invalid email error is displayed",
                resetPasswordPage.waitForErrors(),
                hasItem("not a well-formed email address"));

        // Both are valid, but show seemingly at random
        assertThat(
                resetPasswordPage.getErrors().get(0),
                either(equalTo("size must be between 3 and 20")).or(
                        equalTo("must match ^[a-z\\d_]{3,20}$")));

    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emptyResetPasswordFieldEntries() {
        SignInPage signInPage =
                new BasicWorkFlow().goToHome().clickSignInLink();
        ResetPasswordPage resetPasswordPage = signInPage.goToResetPassword();
        resetPasswordPage = resetPasswordPage.clearFields();
        resetPasswordPage = resetPasswordPage.resetFailure();

        assertThat("Empty email error is displayed",
                resetPasswordPage.waitForErrors(), hasItem("may not be empty"));

        // All are valid, but may show at random
        assertThat(
                resetPasswordPage.getErrors().get(0),
                either(equalTo("size must be between 3 and 20")).or(
                        equalTo("may not be empty")).or(
                        equalTo("must match ^[a-z\\d_]{3,20}$")));

    }

}
