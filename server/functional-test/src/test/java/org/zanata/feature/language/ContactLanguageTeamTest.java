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
import org.zanata.feature.Trace;
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

    @Trace(summary = "The user can contact a language team coordinator")
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
                .as("One email was sent")
                .isGreaterThanOrEqualTo(1);

        WiserMessage wiserMessage = messages.get(0);

        assertThat(wiserMessage.getEnvelopeReceiver())
                .as("The email recipient is the administrator")
                .isEqualTo("admin@example.com");

        String content = HasEmailRule.getEmailContent(wiserMessage);

        assertThat(content)
                .contains("Dear Administrator")
                .as("The email is to the language team coordinator")
                .contains("There is no coordinator for");
        assertThat(languagesPage.getNotificationMessage())
                .as("The user is informed the message was sent")
                .contains("Your message has been sent to the administrator");
    }
}
