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
package org.zanata.feature.language

import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.util.HasEmailExtension
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
@ExtendWith(HasEmailExtension::class)
class ContactLanguageTeamTest : ZanataTestCase() {

    @Trace(summary = "The user can contact a language team coordinator")
    @Test
    fun translatorContactsLanguageTeamCoordinator() {
        val languagesPage = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToLanguages()
                .gotoLanguagePage("fr")
                .clickMoreActions()
                .clickContactCoordinatorsButton()
                .enterMessage("I love Zanata")
                .clickSend()

        val messages = hasEmailExtension.messages

        assertThat(messages.size)
                .describedAs("One email was sent")
                .isGreaterThanOrEqualTo(1)

        val wiserMessage = messages[0]

        assertThat(wiserMessage.envelopeReceiver)
                .describedAs("The email recipient is the administrator")
                .isEqualTo("admin@example.com")

        val content = HasEmailExtension.getEmailContent(wiserMessage)

        assertThat(content)
                .contains("Dear Administrator")
                .describedAs("The email is to the language team coordinator")
                .contains("There is no coordinator for")
        assertThat(languagesPage.notificationMessage)
                .describedAs("The user is informed the message was sent")
                .contains("Your message has been sent to the administrator")
    }
}
