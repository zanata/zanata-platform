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
package org.zanata.feature.misc

import org.junit.jupiter.api.Test
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.more.MorePage
import org.zanata.util.HasEmailExtension
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@DetailedTest
class ContactAdminTest : ZanataTestCase() {

    @Trace(summary = "The user can contact the site administrator")
    @Test
    fun testContactAdmin() {
        val contactAdminFormPage = LoginWorkFlow()
                .signIn("translator", "translator")
                .gotoMorePage()
                .clickContactAdmin()

        val morePage = contactAdminFormPage
                .inputMessage("I love Zanata")
                .send(MorePage::class.java)

        assertThat(verifySentEmail(morePage))
                .contains("You are receiving this mail because:")
                .contains("You are an administrator")
                .contains("I love Zanata")
    }

    @Trace(summary = "The user can contact the site administrator")
    @Test
    fun testAnonymousContactAdmin() {
        val contactAdminFormPage = BasicWorkFlow()
                .goToHome()
                .gotoMorePage()
                .clickContactAdmin()

        val morePage = contactAdminFormPage
                .inputMessage("I love Zanata")
                .sendAnonymous(MorePage::class.java)

        assertThat(verifySentEmail(morePage))
                .contains("Anonymous user from IP address")
                .contains("I love Zanata")
    }

    // Verify the email is sent and received
    private fun verifySentEmail(morePage: MorePage): String {
        assertThat(morePage.expectNotification("Your message has " +
                "been sent to the administrator"))
                .describedAs("An email sent notification shows")
                .isTrue()

        val messages = ZanataTestCase.hasEmailExtension.messages

        assertThat(messages)
                .describedAs("One email was sent")
                .hasSize(1)

        val wiserMessage = messages[0]

        assertThat(wiserMessage.envelopeReceiver)
                .describedAs("The email recipient is the administrator")
                .isEqualTo("admin@example.com")

        return HasEmailExtension.getEmailContent(wiserMessage)
    }
}
