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
package org.zanata.feature.administration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.administration.RoleAssignmentsPage;
import org.zanata.util.AddUsersRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class AutoRoleAssignmentTest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();

    @Feature(summary = "The administrator can create a rule to assign roles " +
            "at user sign in",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void createAutoRoleAssignment() throws Exception {
        RoleAssignmentsPage roleAssignmentsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToAdministration()
                .goToManageRoleAssignments()
                .clickCreateNew()
                .enterIdentityPattern(".+ransla.+")
                .selectRole("admin")
                .saveRoleAssignment();

        assertThat(roleAssignmentsPage.getRulesByPattern())
                .contains(".+ransla.+")
                .as("The rule was created");

        roleAssignmentsPage.logout();
        {
            // TODO: Bug? Remove me
            new LoginWorkFlow()
                    .signIn("translator", "translator")
                    .logout();
        }
        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToAdministration()
                .getTitle()).isEqualTo("Zanata: Administration")
                .as("The translator user was automatically given admin rights");
    }
}
