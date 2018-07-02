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

import org.assertj.core.api.Assertions.assertThat

import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.feature.Trace
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.page.account.InactiveAccountPage
import org.zanata.util.HasEmailRule
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.RegisterWorkFlow

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Category(DetailedTest::class)
class InactiveUserLoginCTest : ZanataTestCase() {
    @get:Rule
    val hasEmailRule = HasEmailRule()

    @Trace(summary = "The user can update the account activation email address",
            testCaseIds = [5696])
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(Exception::class)
    fun updateActivationEmail() {
        val usernamepassword = "tester3"
        RegisterWorkFlow().registerInternal(usernamepassword,
                usernamepassword, usernamepassword,
                "$usernamepassword@example.com")
        var inactiveAccountPage = LoginWorkFlow()
                .signInInactive(usernamepassword, usernamepassword)

        assertThat(inactiveAccountPage.title)
                .`as`("The account is inactive")
                .isEqualTo("Zanata: Account is not activated")

        inactiveAccountPage = inactiveAccountPage
                .enterNewEmail("notproper@")
                .updateEmailFailure()

        assertThat(inactiveAccountPage.errors)
                .contains("not a well-formed email address")

        inactiveAccountPage = inactiveAccountPage
                .enterNewEmail("admin@example.com")
                .updateEmailFailure()

        assertThat(inactiveAccountPage.errors)
                .contains("This email address is already taken.")
    }
}
