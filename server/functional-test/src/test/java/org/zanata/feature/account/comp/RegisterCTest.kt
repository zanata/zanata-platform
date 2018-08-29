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
package org.zanata.feature.account.comp

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.RegisterPage
import org.zanata.page.utility.HomePage
import org.zanata.workflow.BasicWorkFlow
import java.util.Random
import org.assertj.core.api.Assertions.assertThat
import org.zanata.feature.testharness.ComprehensiveTest

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@ComprehensiveTest
class RegisterCTest : ZanataTestCase() {
    private lateinit var homePage: HomePage

    @BeforeEach
    fun before() {
        homePage = BasicWorkFlow().goToHome()
    }

    @Trace(summary = "The user must enter all necessary fields to register",
            testPlanIds = [5681], testCaseIds = [5689, 5690, 5691, 5692])
    @Test
    fun requiredFields() {
        val fields = HashMap<String, String>()
        fields["name"] = ""
        fields["username"] = ""
        fields["email"] = ""
        fields["password"] = ""
        val registerPage = homePage
                .goToRegistration()
                .setFields(fields)
                .registerFailure()

        assertThat(registerPage.getErrors(4))
                .describedAs("Size indication or 'May not be empty' shows for all fields")
                .containsExactly(RegisterPage.REQUIRED_FIELD_ERROR,
                        RegisterPage.REQUIRED_FIELD_ERROR,
                        RegisterPage.MALFORMED_EMAIL_ERROR,
                        RegisterPage.PASSWORD_LENGTH_ERROR)
    }

    @Trace(summary = "The user can navigate to Login from Sign up, or to " +
            "Sign up from Login", testCaseIds = [-1])
    @Test
    fun signUpToLoginAndBack() {
        val registerPage = homePage
                .clickSignInLink()
                .goToRegister()

        assertThat(registerPage.pageTitle)
                .describedAs("The user is sent to the register page")
                .isEqualTo("Sign up with Zanata")

        assertThat(registerPage.goToSignIn().pageTitle)
                .describedAs("The user is sent to the log in page")
                .isEqualTo("Log in with your username")
    }

    @Trace(summary = "The user can show or hide the registration password content",
            testCaseIds = [-1])
    @Test
    fun togglePasswordVisible() {
        var registerPage = homePage
                .goToRegistration()
                .enterPassword("mypassword")

        assertThat(registerPage.passwordFieldType)
                .describedAs("The password field starts as masked")
                .isEqualTo("password")

        registerPage = registerPage.clickPasswordShowToggle()

        assertThat(registerPage.passwordFieldType)
                .describedAs("The password field is now not masked")
                .isEqualTo("text")

        registerPage = registerPage.clickPasswordShowToggle()

        assertThat(registerPage.passwordFieldType)
                .describedAs("The password field is again masked")
                .isEqualTo("password")
        assertThat(registerPage.password)
                .describedAs("The password field did not lose the entered text")
                .isEqualTo("mypassword")
    }

    @Trace(summary = "The user must provide a password to register via " +
            "internal authentication", testCaseIds = [5692])
    @Test
    fun passwordLengthValidation() {
        val longPass = makeString(1030)
        assertThat(longPass.length).isGreaterThan(1024)

        var registerPage = homePage
                .goToRegistration()
                .enterName("jimmy")
                .enterEmail("jimmy@jim.net")
                .enterUserName("jimmy")
                .enterPassword("A")
                .registerFailure()

        assertThat(registerPage.errors)
                .describedAs("Password requires at least 6 characters")
                .contains(RegisterPage.PASSWORD_LENGTH_ERROR)

        registerPage = registerPage.enterPassword(longPass).registerFailure()

        assertThat(registerPage.errors)
                .describedAs("The user must enter a password of at most 1024 characters")
                .contains(RegisterPage.PASSWORD_LENGTH_ERROR)
    }

    @Trace(summary = "The user must provide a name to register",
            testPlanIds = [5681], testCaseIds = [5689])
    @Test
    fun userMustSpecifyAValidName() {
        val longName = makeString(81)
        assertThat(longName.length).isGreaterThan(80)

        var registerPage = homePage
                .goToRegistration()
                .enterName("A")
                .enterUserName("usermustspecifyaname")
                .enterEmail("userMustSpecifyAName@test.com")
                .enterPassword("password")
                .registerFailure()

        assertThat(registerPage.errors)
                .describedAs("A name greater than 1 character must be specified")
                .contains(RegisterPage.USER_DISPLAY_NAME_LENGTH_ERROR)

        registerPage = registerPage.enterName(longName).registerFailure()

        assertThat(registerPage.errors)
                .describedAs("A name shorter than 81 characters is specified")
                .contains(RegisterPage.USER_DISPLAY_NAME_LENGTH_ERROR)
    }

    @Trace(summary = "The user must provide a username to register",
            testPlanIds = [5681], testCaseIds = [5690])
    @Test
    fun userMustSpecifyAUsername() {
        val registerPage = homePage
                .goToRegistration()
                .enterName("usernamespecified")
                .enterEmail("userMustSpecifyAUsername@test.com")
                .enterPassword("password")
                .registerFailure()

        assertThat(containsUsernameError(registerPage.errors))
                .describedAs("A username must be specified")
                .isTrue()
    }

    @Trace(summary = "A username cannot be all underscores (RHBZ-981498)")
    @Test
    fun bug981498_underscoreRules() {
        val fields = HashMap<String, String>()
        fields["name"] = "test"
        fields["password"] = "testpassword"
        fields["email"] = "bug981498test@example.com"
        // Username is all underscores
        fields["username"] = "______"
        val registerPage = homePage
                .goToRegistration()
                .setFields(fields)
        registerPage.defocus()

        assertThat(registerPage.errors)
                .describedAs("A username of all underscores is not valid")
                .contains(RegisterPage.USERNAME_VALIDATION_ERROR)
    }

    /*
     * Helper function - the error returned may be different depending on
     * the given input.
     */
    private fun containsUsernameError(errors: List<String>): Boolean {
        return errors.contains(RegisterPage.USERNAME_VALIDATION_ERROR) ||
                errors.contains(RegisterPage.USERNAME_LENGTH_ERROR) ||
                errors.contains(RegisterPage.REQUIRED_FIELD_ERROR)
    }

    private fun makeString(length: Int): String {
        val ret = CharArray(length)
        val r = Random()
        for (i in 0 until length) {
            ret[i] = (r.nextInt(26) + 'a'.toInt()).toChar()
        }
        return String(ret)
    }
}
