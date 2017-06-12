/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.account.comp;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.utility.HomePage;
import org.zanata.workflow.BasicWorkFlow;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(TestPlan.ComprehensiveTest.class)
public class RegisterCTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RegisterUsernameCharactersCTest.class);

    private HomePage homePage;

    @Before
    public void before() {
        homePage = new BasicWorkFlow().goToHome();
    }

    @Feature(summary = "The user must enter all necessary fields to register",
            testPlanIds = 5681, testCaseIds = {5689, 5690, 5691, 5692})
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void requiredFields() throws Exception {
        Map<String, String> fields = new HashMap<>();
        fields.put("name", "");
        fields.put("username", "");
        fields.put("email", "");
        fields.put("password", "");
        RegisterPage registerPage = homePage
                .goToRegistration()
                .setFields(fields)
                .registerFailure();

        assertThat(registerPage.getErrors(4)).containsExactly(
                RegisterPage.USERDISPLAYNAME_LENGTH_ERROR,
                RegisterPage.USERNAME_LENGTH_ERROR,
                RegisterPage.REQUIRED_FIELD_ERROR,
                RegisterPage.REQUIRED_FIELD_ERROR)
                .as("Size indication or 'May not be empty' shows for all fields");
    }

    @Feature(summary = "The user can navigate to Login from Sign up, or to "
            + "Sign up from Login", testCaseIds = -1)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void signUpToLoginAndBack() {
        RegisterPage registerPage = homePage
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

    @Feature(summary = "The user can show or hide the registration password content",
            testCaseIds = -1)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void togglePasswordVisible() {
        RegisterPage registerPage = homePage
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

    @Feature(summary = "The user must provide a username to register",
            testPlanIds = 5681, testCaseIds = 5690)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void userMustSpecifyAUsername() {
        RegisterPage registerPage = homePage
                .goToRegistration()
                .enterName("usernamespecified")
                .enterEmail("userMustSpecifyAUsername@test.com")
                .enterPassword("password")
                .registerFailure();

        assertThat(containsUsernameError(registerPage.getErrors()))
                .isTrue()
                .as("A username must be specified");
    }

    @Feature(summary = "A username cannot be all underscores (RHBZ-981498)",
            bugzilla = 981498)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void bug981498_underscoreRules() {
        Map<String, String> fields = new HashMap<>();
        fields.put("name", "test");
        fields.put("password", "testpassword");
        fields.put("email", "bug981498test@example.com");
        // Username is all underscores
        fields.put("username", "______");
        RegisterPage registerPage = homePage
                .goToRegistration()
                .setFields(fields);
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
