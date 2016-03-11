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
package org.zanata.feature.misc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.utility.ContactAdminFormPage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.LoginWorkFlow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ContactAdminTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule emailRule = new HasEmailRule();

    @Feature(summary = "The user can contact the site administrator",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181717)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void testContactAdmin() {
        ContactAdminFormPage contactAdminFormPage = new LoginWorkFlow()
                .signIn("translator", "translator")
                .clickContactAdmin();

        DashboardBasePage dashboardBasePage = contactAdminFormPage
                .inputMessage("I love Zanata")
                .send(DashboardBasePage.class);

        assertThat(dashboardBasePage.expectNotification("Your message has " +
                "been sent to the administrator"))
                .isTrue()
                .as("An email sent notification shows");

        List<WiserMessage> messages = emailRule.getMessages();

        assertThat(messages.size())
                .isGreaterThanOrEqualTo(1)
                .as("One email was sent");

        WiserMessage wiserMessage = messages.get(0);

        assertThat(wiserMessage.getEnvelopeReceiver())
                .isEqualTo("admin@example.com")
                .as("The email recipient is the administrator");

        String content = HasEmailRule.getEmailContent(wiserMessage);

        assertThat(content)
                .contains("You are receiving this mail because:")
                .contains("You are an administrator")
                .contains("I love Zanata");

        /**
         * Check disabled due to concurrency issue with host url in
         * server config. Should enable this when we have rules to clean database
         * on each test.
         *
        // contains instance url (without last slash)
        String instanceUrl = PropertiesHolder.getProperty(
                Constants.zanataInstance.value()).replaceAll("/$", "");

        assertThat(content)
                .contains(instanceUrl)
                .as("The email origin (server) is correct");
         */
    }

}
