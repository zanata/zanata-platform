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
package org.zanata.feature.account

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.RegisterPage
import org.zanata.page.utility.HomePage
import org.zanata.workflow.BasicWorkFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class RegisterTest : ZanataTestCase() {

    private lateinit var fields: MutableMap<String, String>

    @BeforeEach
    fun before() {
        // fields contains a set of data that can be successfully registered
        fields = hashMapOf(
                "email" to "test@example.com",
                "username" to "testusername",
                "name" to "test",
                "password" to "testpassword")
    }

    @Trace(summary = "The user can register an account with Zanata",
            testPlanIds = [5681], testCaseIds = [5688])
    @Test
    @DisplayName("User can register a new account")
    fun `User can register a new account`() {
        val registerPage = BasicWorkFlow().goToHome()
                .goToRegistration()
                .setFields(fields)

        assertThat(registerPage.errors)
                .describedAs("No errors are shown")
                .isEmpty()

        val signInPage = registerPage.register()

        assertThat(signInPage.notificationMessage)
                .describedAs("Sign up is successful")
                .isEqualTo(HomePage.SIGNUP_SUCCESS_MESSAGE)
    }

    @Trace(summary = "The user must enter a username of between 3 and " +
            "20 (inclusive) characters to register",
            testPlanIds = [5681], testCaseIds = [5690])
    @Test
    @DisplayName("Registration fails on incorrect username length")
    fun `Registration fails on incorrect username length`() {
        fields["email"] = "length.test@example.com"
        var registerPage = BasicWorkFlow().goToHome().goToRegistration()

        fields["username"] = "bo"
        registerPage = registerPage.setFields(fields)

        assertThat(containsUsernameError(registerPage.errors))
                .describedAs("Size errors are shown for string too short")
                .isTrue()

        fields["username"] = "testusername"
        registerPage = registerPage.setFields(fields)

        assertThat(containsUsernameError(registerPage.errors))
                .describedAs("Size errors are not shown")
                .isFalse()

        fields["username"] = "12345678901234567890a"
        registerPage = registerPage.setFields(fields)

        assertThat(containsUsernameError(registerPage.errors))
                .describedAs("Size errors are shown for string too long")
                .isTrue()
    }

    @Trace(summary = "The user must enter a unique username to register",
            testPlanIds = [5681], testCaseIds = [5690])
    @Test
    @DisplayName("Registration fails on taken username")
    fun `Registration fails on taken username`() {
        val registerPage = BasicWorkFlow().goToHome()
                .goToRegistration()
                .enterUserName("admin")
        registerPage.defocus(registerPage.usernameField)

        assertThat(registerPage.errors)
                .describedAs("Username not available message is shown")
                .contains(RegisterPage.USERNAME_UNAVAILABLE_ERROR)
    }

    /*
     * Helper function - the error returned may be different depending on
     * the given input.
     */
    private fun containsUsernameError(errors: List<String>): Boolean {
        return errors.contains(RegisterPage.USERNAME_VALIDATION_ERROR)
                || errors.contains(RegisterPage.USERNAME_LENGTH_ERROR)
    }
}
