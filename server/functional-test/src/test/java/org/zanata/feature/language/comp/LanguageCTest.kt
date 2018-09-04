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

package org.zanata.feature.language.comp

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.ComprehensiveTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.util.HasEmailExtension
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Sachin Pathare [spathare@redhat.com](mailto:spathare@redhat.com)
 */
@ComprehensiveTest
class LanguageCTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        BasicWorkFlow().goToHome().deleteCookiesAndRefresh()
        assertThat(LoginWorkFlow().signIn("translator", "translator")
                .loggedInAs())
                .describedAs("translator is logged in")
                .isEqualTo("translator")
    }

    @Trace(summary = "Translator can search for language",
            testPlanIds = [5681], testCaseIds = [5786])
    @Test
    fun searchLanguage() {
        val language = "fr"
        val languagesPage = BasicWorkFlow()
                .goToHome()
                .goToLanguages()

        assertThat(languagesPage.languageLocales)
                .describedAs("The language is listed")
                .contains(language)

    }

    @Trace(summary = "Translator can request to join language team",
            testPlanIds = intArrayOf(5681), testCaseIds = intArrayOf(5795, 5796))
    @Test
    fun requestToJoinLanguage() {
        val language = "en-US"
        val languagePage = BasicWorkFlow()
                .goToHome()
                .goToLanguages()
                .gotoLanguagePage(language)
                .requestToJoin()
                .enterMessage("I want to join this language team")
                .clickSend()

        val messages = hasEmailExtension.messages

        assertThat(messages.size)
                .describedAs("One email was sent")
                .isGreaterThanOrEqualTo(1)

        val wiserMessage = messages[0]

        assertThat(wiserMessage.envelopeReceiver)
                .describedAs("The email recipient is the Coordinator")
                .isEqualTo("admin@example.com")

        val content = HasEmailExtension.getEmailContent(wiserMessage)

        assertThat(content)
                .contains("Dear Language Team Coordinator")
                .contains(
                        "Zanata user \"translator\" with id \"translator\" is requesting to join ")
                .describedAs("The email is to the language team coordinator")

        assertThat(languagePage.notificationMessage)
                .describedAs("The user is informed the message was sent")
                .contains("Your message has been sent to the administrator")

    }

    @Trace(summary = "Translator can cancel request",
            testPlanIds = intArrayOf(5681), testCaseIds = intArrayOf(5796))
    @Test
    fun cancelRequest() {
        val language = "en-US"
        val languagePage = BasicWorkFlow()
                .goToHome()
                .goToLanguages()
                .gotoLanguagePage(language)
                .requestToJoin()
                .enterMessage("I want to join this language team")
                .clickSend()
                .cancelRequest()

        assertThat(languagePage.notificationMessage)
                .describedAs("Request to join language team is cancel")
                .contains("Request cancelled by translator")
    }

    @Trace(summary = "Translator can leave language team",
            testPlanIds = intArrayOf(5681), testCaseIds = intArrayOf(5800))
    @Test
    fun leaveLanguageTeam() {
        val language = "fr"
        val languagePage = BasicWorkFlow()
                .goToHome()
                .goToLanguages()
                .gotoLanguagePage(language)
                .leaveTeam()

        assertThat(languagePage.notificationMessage)
                .describedAs("Leaved language team $language")
                .contains("You have left the français language team")
    }

}
