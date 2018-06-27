/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.administration

import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.feature.testharness.TestPlan
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.RegisterPage
import org.zanata.page.administration.AdministrationPage
import org.zanata.page.administration.ServerConfigurationPage
import org.zanata.page.more.MorePage
import org.zanata.page.utility.HomePage
import org.zanata.util.HasEmailRule
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.RegisterWorkFlow
import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Category(TestPlan.DetailedTest::class)
class ServerSettingsTest : ZanataTestCase() {
    @get:Rule
    val hasEmailRule = HasEmailRule()

    @Test
    @Ignore("unstable")
    fun setServerURLTest() {
        LoginWorkFlow().signIn("admin", "admin").goToAdministration()
                .goToServerConfigPage()
                .inputServerURL("http://myserver.com/zanata").save()
                .gotoMorePage().clickContactAdmin().inputMessage("Test pattern")
                .send(HomePage::class.java)
        val emailContent = HasEmailRule.getEmailContent(hasEmailRule.messages[0])
        assertThat(emailContent)
                .describedAs("The email indicates the expected server url")
                .contains("http://myserver.com/zanata")
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun setRegisterURLTest() {
        val url = "http://myserver.com/register"
        val serverConfigurationPage = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .inputRegisterURL(url)
                .save()
                .goToServerConfigPage()

        // check that expected url was displayed
        serverConfigurationPage.expectFieldValue(
                ServerConfigurationPage.registerUrlField, url)
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun setAdministratorEmailTest() {
        val administrationPage = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .inputAdminEmail("lara@example.com")
                .save()
        // Intermittent test error
        administrationPage.reload()
        administrationPage.gotoMorePage()
                .clickContactAdmin()
                .inputMessage("Test pattern")
                .send(HomePage::class.java)

        assertThat(hasEmailRule.messages[0].envelopeReceiver)
                .describedAs("The recipient admin was set")
                .contains("lara@example.com")
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Ignore("unstable")
    fun setAdministratorEmailFromTest() {
        val email = "lara@example.com"
        val serverConfigurationPage = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .inputAdminFromEmail(email)
                .save()
                .goToServerConfigPage()

        serverConfigurationPage.expectFieldValue(
                ServerConfigurationPage.fromEmailField, email)

        serverConfigurationPage.goToHomePage().logout()
        RegisterWorkFlow().registerInternal("test1", "test1", "test123",
                "test1@test.com")

        assertThat(hasEmailRule.messages[0].envelopeSender)
                .describedAs("The server email sender was set")
                .contains("lara@example.com")
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun setHelpURLTest() {
        val morePage = LoginWorkFlow().signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .inputHelpURL("http://www.test.com")
                .save()
                .gotoMorePage()

        assertThat(morePage.helpURL)
                .describedAs("The help URL was set correctly")
                .isEqualTo("http://www.test.com/")
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun unsetTermsOfUseURL() {
        val registerPage = LoginWorkFlow().signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .inputTermsOfUseURL("http://www.test.com")
                .save()
                .goToServerConfigPage()
                .inputTermsOfUseURL("")
                .save()
                .logout()
                .goToRegistration()

        assertThat(registerPage.termsOfUseUrlVisible())
                .describedAs("The Terms of Use URL is not visible")
                .isFalse()
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun setTermsOfUseURLTest() {
        val registerPage = LoginWorkFlow().signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .inputTermsOfUseURL("http://www.test.com")
                .save()
                .logout()
                .goToRegistration()

        assertThat(registerPage.termsUrl)
                .describedAs("The Terms of Use URL was set correctly")
                .isEqualTo("http://www.test.com/")
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun setEmailLoggingTest() {
        val serverConfigurationPage = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .clickLoggingEnabledCheckbox()
                .selectLoggingLevel("Error")
                .inputLogEmailTarget("lara@example.com")
                .save()
                .goToServerConfigPage()

        assertThat(serverConfigurationPage.selectedLoggingLevel())
                .describedAs("Level is correct")
                .isEqualTo("Error")
        assertThat(serverConfigurationPage.logEmailTarget)
                .describedAs("Recipient is correct")
                .isEqualTo("lara@example.com")
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun setPiwikTest() {
        val serverConfigurationPage = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .inputPiwikUrl("http://example.com/piwik")
                .inputPiwikID("12345")
                .save()
                .goToServerConfigPage()

        assertThat(serverConfigurationPage.piwikUrl)
                .describedAs("Piwik url is correct is correct")
                .isEqualTo("http://example.com/piwik")
        assertThat(serverConfigurationPage.piwikID)
                .describedAs("Piwik ID is correct")
                .isEqualTo("12345")
    }
}
