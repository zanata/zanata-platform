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

package org.zanata.feature.language.comp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.languages.LanguagePage;
import org.zanata.page.languages.LanguagesPage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sachin Pathare <a
 *         href="mailto:spathare@redhat.com">spathare@redhat.com</a>
 */
@Category(TestPlan.ComprehensiveTest.class)
public class LanguageCTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule emailRule = new HasEmailRule();

    @Before
    public void before() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        assertThat(new LoginWorkFlow().signIn("translator", "translator").loggedInAs())
                .isEqualTo("translator")
                .as("translator is logged in");
    }

    @Trace(summary = "Translator can search for language",
            testPlanIds = 5681, testCaseIds = {5786})
    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void searchLanguage() throws Exception {
        String language = "fr";
        LanguagesPage languagesPage = new BasicWorkFlow()
                .goToHome()
                .goToLanguages();

        assertThat(languagesPage.getLanguageLocales())
                .contains(language)
                .as("The language is listed");

    }

    @Trace(summary = "Translator can request to join language team",
            testPlanIds = 5681, testCaseIds = {5795, 5796})
    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void requestToJoinLanguage() throws Exception {
        String language = "en-US";
        LanguagePage languagePage = new BasicWorkFlow()
                .goToHome()
                .goToLanguages()
                .gotoLanguagePage(language)
                .requestToJoin()
                .enterMessage("I want to join this language team")
                .clickSend();

        List<WiserMessage> messages = emailRule.getMessages();

        assertThat(messages.size())
                .isGreaterThanOrEqualTo(1)
                .as("One email was sent");

        WiserMessage wiserMessage = messages.get(0);

        assertThat(wiserMessage.getEnvelopeReceiver())
                .isEqualTo("admin@example.com")
                .as("The email recipient is the Coordinator");

        String content = HasEmailRule.getEmailContent(wiserMessage);

        assertThat(content)
                .contains("Dear Language Team Coordinator")
                .contains("Zanata user \"translator\" with id \"translator\" is requesting to join ")
                .as("The email is to the language team coordinator");

        assertThat(languagePage.getNotificationMessage())
                .contains("Your message has been sent to the administrator")
                .as("The user is informed the message was sent");

    }

    @Trace(summary = "Translator can cancel request",
            testPlanIds = 5681, testCaseIds = {5796})
    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void cancelRequest() throws Exception {
        String language = "en-US";
        LanguagePage languagePage = new BasicWorkFlow()
                .goToHome()
                .goToLanguages()
                .gotoLanguagePage(language)
                .requestToJoin()
                .enterMessage("I want to join this language team")
                .clickSend()
                .cancelRequest();

        assertThat(languagePage.getNotificationMessage())
                .contains("Request cancelled by translator")
                .as("Request to join language team is cancel");
    }

    @Trace(summary = "Translator can leave language team",
            testPlanIds = 5681, testCaseIds = {5800})
    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void leaveLanguageTeam() throws Exception {
        String language = "fr";
        LanguagePage languagePage = new BasicWorkFlow()
                .goToHome()
                .goToLanguages()
                .gotoLanguagePage(language)
                .leaveTeam();

        assertThat(languagePage.getNotificationMessage())
                .contains("You have left the français language team")
                .as("Leaved language team " + language);
    }

}
