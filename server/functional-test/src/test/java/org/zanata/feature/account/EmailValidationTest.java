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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.RegisterPage;
import org.zanata.workflow.BasicWorkFlow;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EmailValidationTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EmailValidationTest.class);

    private RegisterPage registerPage;

    @BeforeClass
    public static void setUp() {
        // Ensure no login
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
    }

    @Before
    public void before() {
        registerPage = new BasicWorkFlow().goToHome().goToRegistration();
    }

    @Feature(
            summary = "The system will allow acceptable forms of an email address for registration",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test
    public void validEmailAcceptance() throws Exception {
        registerPage =
                // Shift to other field
                registerPage.enterEmail("me@mydomain.com")
                        .enterName("Sam I Am");
        assertThat(registerPage.getErrors())
                .as("Email validation errors are not shown").isEmpty();
    }

    @Feature(
            summary = "The user must enter a valid email address to register with Zanata",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test
    public void invalidEmailRejection() throws Exception {
        registerPage = registerPage.enterEmail("plaintext").registerFailure();
        assertThat(registerPage.getErrors())
                .contains(RegisterPage.MALFORMED_EMAIL_ERROR)
                .as("The email formation error is displayed");
    }
}
