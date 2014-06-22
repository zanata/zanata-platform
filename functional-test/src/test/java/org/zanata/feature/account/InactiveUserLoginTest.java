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
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.account.SignInPage;
import org.zanata.util.AddUsersRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Category(DetailedTest.class)
public class InactiveUserLoginTest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();

    @Feature(summary = "The user needs to verify their account before they may " +
            "log in",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181714)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void loginWithInactiveUser() throws Exception {
        new LoginWorkFlow().signIn("admin", "admin").goToAdministration()
                .goToManageUserPage().editUserAccount("translator")
                .clickEnabled().saveUser().logout();

        SignInPage signInPage = new LoginWorkFlow()
                .signInFailure("translator", "translator");
        assertThat(signInPage.getNotificationMessage())
                .isEqualTo(SignInPage.LOGIN_FAILED_ERROR)
                .as("The inactive user cannot log in");
    }

}
