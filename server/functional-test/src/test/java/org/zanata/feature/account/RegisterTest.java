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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.BasicWorkFlow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class RegisterTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule emailRule = new HasEmailRule();

    Map<String, String> fields;
    private HomePage homePage;

    @Before
    public void before() {
        // fields contains a set of data that can be successfully registered
        fields = new HashMap<String, String>();

        // Conflicting fields - must be set for each test function to avoid
        // "not available" errors
        fields.put("email", "test@example.com");
        fields.put("username", "testusername");
        fields.put("name", "test");
        fields.put("password", "testpassword");
        homePage = new BasicWorkFlow().goToHome();
        homePage.deleteCookiesAndRefresh();
    }

    @Feature(summary = "The user can register an account with Zanata",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86816)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void registerSuccessful() throws Exception {
        RegisterPage registerPage = homePage
                .goToRegistration()
                .setFields(fields);

        assertThat(registerPage.getErrors())
                .as("No errors are shown")
                .isEmpty();

        SignInPage signInPage = registerPage.register();

        assertThat(signInPage.getNotificationMessage())
                .isEqualTo(HomePage.SIGNUP_SUCCESS_MESSAGE)
                .as("Sign up is successful");
    }

    @Feature(summary = "The user must enter a username of between 3 and " +
            "20 (inclusive) characters to register",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void usernameLengthValidation() throws Exception {
        fields.put("email", "length.test@example.com");
        RegisterPage registerPage = homePage.goToRegistration();

        fields.put("username", "bo");
        registerPage = registerPage.setFields(fields);

        assertThat(containsUsernameError(registerPage.getErrors()))
                .isTrue()
                .as("Size errors are shown for string too short");

        fields.put("username", "testusername");
        registerPage = registerPage.setFields(fields);

        assertThat(containsUsernameError(registerPage.getErrors()))
                .isFalse()
                .as("Size errors are not shown");

        fields.put("username", "12345678901234567890a");
        registerPage = registerPage.setFields(fields);

        assertThat(containsUsernameError(registerPage.getErrors()))
                .isTrue()
                .as("Size errors are shown for string too long");
    }

    @Feature(summary = "The user must enter a unique username to register",
        tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void usernamePreExisting() throws Exception {
        RegisterPage registerPage = homePage
                .goToRegistration()
                .enterUserName("admin");
        registerPage.defocus(registerPage.usernameField);

        assertThat(registerPage.getErrors())
                .contains(RegisterPage.USERNAME_UNAVAILABLE_ERROR)
                .as("Username not available message is shown");
    }

    @Feature(summary = "The user must enter all necessary fields to register",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Ignore("RHBZ-1024150")
    public void requiredFields() throws Exception {
        fields.put("name", "");
        fields.put("username", "");
        fields.put("email", "");
        fields.put("password", "");
        RegisterPage registerPage =
                homePage.goToRegistration().setFields(fields);
        registerPage.defocus();

        assertThat(registerPage.getErrors(4)).containsExactly(
                RegisterPage.REQUIRED_FIELD_ERROR,
                RegisterPage.USERNAME_VALIDATION_ERROR,
                RegisterPage.REQUIRED_FIELD_ERROR,
                RegisterPage.REQUIRED_FIELD_ERROR)
                .as("Value is required shows for all fields");
    }

    @Feature(summary = "The user can access the login page from the register " +
            "page, and vice versa",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test
    public void signUpToLoginAndBack() {
        RegisterPage registerPage = new BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .goToRegister();

        assertThat(registerPage.getPageTitle())
                .isEqualTo("Sign up with Zanata")
                .as("The user is sent to the register page");

        SignInPage signInPage = registerPage.goToSignIn();

        assertThat(signInPage.getPageTitle())
                .isEqualTo("Log in with your username")
                .as("The user is sent to the log in page");
    }

    @Feature(summary = "The user can toggle the entered password visible and " +
            "masked",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test
    public void togglePasswordVisible() {
        RegisterPage registerPage = new BasicWorkFlow()
                .goToHome()
                .goToRegistration()
                .enterPassword("mypassword");

        assertThat(registerPage.getPasswordFieldType())
                .isEqualTo("password")
                .as("The password field starts as masked");

        registerPage = registerPage.clickPasswordShowToggle();

        assertThat(registerPage.getPasswordFieldType())
                .isEqualTo("text")
                .as("The password field is now not masked");

        registerPage = registerPage.clickPasswordShowToggle();

        assertThat(registerPage.getPasswordFieldType())
                .isEqualTo("password")
                .as("The password field is again masked");
        assertThat(registerPage.getPassword())
                .isEqualTo("mypassword")
                .as("The password field did not lose the entered text");
    }

    @Feature(summary = "The user must enter at least one alphanumeric " +
            "character in their username",
            bugzilla = 981498)
    @Test
    public void bug981498_underscoreRules() {
        fields.put("email", "bug981498test@example.com");
        fields.put("username", "______");
        RegisterPage registerPage =
                homePage.goToRegistration().setFields(fields);
        registerPage.defocus();

        assertThat(registerPage.getErrors())
                .contains(RegisterPage.USERNAME_VALIDATION_ERROR)
                .as("A username of all underscores is not valid");
    }

    /*
     * Helper function - the error returned may be different depending on
     * the given input.
     */
    private boolean containsUsernameError(List<String> errors) {
        return errors.contains(RegisterPage.USERNAME_VALIDATION_ERROR) ||
                errors.contains(RegisterPage.USERNAME_LENGTH_ERROR);
    }
}
