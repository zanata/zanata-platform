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
package org.zanata.feature.language;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.languages.LanguagesPage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.LoginWorkFlow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ContactLanguageTeamTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule emailRule = new HasEmailRule();

    @Feature(summary = "The user can contact a language team coordinator",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void translatorContactsLanguageTeamCoordinator() throws Exception {
        LanguagesPage languagesPage = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToLanguages()
                .gotoLanguagePage("fr")
                .clickMoreActions()
                .clickContactCoordinatorsButton()
                .enterMessage("I love Zanata")
                .clickSend();

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
                .contains("Dear Administrator")
                .contains("There is no coordinator for")
                .as("The email is to the language team coordinator");
        assertThat(languagesPage.getNotificationMessage())
                .contains("Your message has been sent to the administrator")
                .as("The user is informed the message was sent");
    }
}
