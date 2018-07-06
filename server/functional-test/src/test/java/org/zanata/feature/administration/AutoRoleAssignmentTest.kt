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
package org.zanata.feature.administration

import org.junit.jupiter.api.Test
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class AutoRoleAssignmentTest : ZanataTestCase() {

    @Trace(summary = "The administrator can create a rule to assign roles " +
            "at user sign in")
    @Test
    @DisplayName("Created rules change user's role at login")
    fun `Created rules change user's role at login`() {
        val roleAssignmentsPage = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToAdministration()
                .goToManageRoleAssignments()
                .clickMoreActions()
                .clickCreateNew()
                .enterIdentityPattern(".+ransla.+")
                .selectRole("admin")
                .saveRoleAssignment()

        assertThat(roleAssignmentsPage.rulesByPattern)
                .describedAs("The rule was created")
                .contains(".+ransla.+")

        roleAssignmentsPage.logout()
        run {
            // TODO: ZNTA-440
            LoginWorkFlow()
                    .signIn("translator", "translator")
                    .logout()
        }
        assertThat(LoginWorkFlow()
                .signIn("translator", "translator")
                .goToAdministration().title)
                .describedAs("The translator user was automatically given admin rights")
                .isEqualTo("Zanata: Administration")
    }
}
