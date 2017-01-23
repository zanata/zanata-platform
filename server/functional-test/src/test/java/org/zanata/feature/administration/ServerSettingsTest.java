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
package org.zanata.feature.administration;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.administration.ServerConfigurationPage;
import org.zanata.page.more.MorePage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.RegisterWorkFlow;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(TestPlan.DetailedTest.class)
public class ServerSettingsTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ServerSettingsTest.class);

    @Rule
    public final HasEmailRule hasEmailRule = new HasEmailRule();

    @Test
    @Ignore("unstable")
    public void setServerURLTest() {
        new LoginWorkFlow().signIn("admin", "admin").goToAdministration()
                .goToServerConfigPage()
                .inputServerURL("http://myserver.com/zanata").save()
                .gotoMorePage().clickContactAdmin().inputMessage("Test pattern")
                .send(HomePage.class);
        String emailContent =
                HasEmailRule.getEmailContent(hasEmailRule.getMessages().get(0));
        assertThat(emailContent).contains("http://myserver.com/zanata")
                .as("The email indicates the expected server url");
    }

    @Test
    public void setRegisterURLTest() {
        String url = "http://myserver.com/register";
        ServerConfigurationPage serverConfigurationPage =
                new LoginWorkFlow().signIn("admin", "admin")
                        .goToAdministration().goToServerConfigPage()
                        .inputRegisterURL(url).save().goToServerConfigPage();
        assertThat(serverConfigurationPage.expectFieldValue(
                ServerConfigurationPage.registerUrlField, url))
                        .as("The expected url was displayed");
    }

    @Test
    public void setAdministratorEmailTest() {
        new LoginWorkFlow().signIn("admin", "admin").goToAdministration()
                .goToServerConfigPage().inputAdminEmail("lara@example.com")
                .save().gotoMorePage().clickContactAdmin()
                .inputMessage("Test pattern").send(HomePage.class);
        assertThat(hasEmailRule.getMessages().get(0).getEnvelopeReceiver())
                .contains("lara@example.com").as("The recipient admin was set");
    }

    @Test
    @Ignore("unstable")
    public void setAdministratorEmailFromTest() {
        String email = "lara@example.com";
        ServerConfigurationPage serverConfigurationPage = new LoginWorkFlow()
                .signIn("admin", "admin").goToAdministration()
                .goToServerConfigPage().inputAdminFromEmail(email).save()
                .goToServerConfigPage();
        assertThat(serverConfigurationPage.expectFieldValue(
                ServerConfigurationPage.fromEmailField, email));
        serverConfigurationPage.goToHomePage().logout();
        new RegisterWorkFlow().registerInternal("test1", "test1", "test123",
                "test1@test.com");
        assertThat(hasEmailRule.getMessages().get(0).getEnvelopeSender())
                .contains("lara@example.com")
                .as("The server email sender was set");
    }

    @Test
    public void setHelpURLTest() {
        MorePage morePage = new LoginWorkFlow().signIn("admin", "admin")
                .goToAdministration().goToServerConfigPage()
                .inputHelpURL("http://www.test.com").save().gotoMorePage();
        assertThat(morePage.getHelpURL()).isEqualTo("http://www.test.com/")
                .as("The help URL was set correctly");
    }

    @Test
    public void unsetTermsOfUseURL() {
        RegisterPage registerPage = new LoginWorkFlow().signIn("admin", "admin")
                .goToAdministration().goToServerConfigPage()
                .inputTermsOfUseURL("http://www.test.com").save()
                .goToServerConfigPage().inputTermsOfUseURL("").save().logout()
                .goToRegistration();
        assertThat(registerPage.termsOfUseUrlVisible()).isFalse()
                .as("The Terms of Use URL is not visible");
    }

    @Test
    public void setTermsOfUseURLTest() {
        RegisterPage registerPage = new LoginWorkFlow().signIn("admin", "admin")
                .goToAdministration().goToServerConfigPage()
                .inputTermsOfUseURL("http://www.test.com").save().logout()
                .goToRegistration();
        assertThat(registerPage.getTermsUrl()).isEqualTo("http://www.test.com/")
                .as("The Terms of Use URL was set correctly");
    }

    @Test
    public void setEmailLoggingTest() {
        ServerConfigurationPage serverConfigurationPage = new LoginWorkFlow()
                .signIn("admin", "admin").goToAdministration()
                .goToServerConfigPage().clickLoggingEnabledCheckbox()
                .selectLoggingLevel("Error")
                .inputLogEmailTarget("lara@example.com").save()
                .goToServerConfigPage();
        assertThat(serverConfigurationPage.selectedLoggingLevel())
                .isEqualTo("Error").as("Level is correct");
        assertThat(serverConfigurationPage.getLogEmailTarget())
                .isEqualTo("lara@example.com").as("Recipient is correct");
    }

    @Test
    public void setPiwikTest() {
        ServerConfigurationPage serverConfigurationPage =
                new LoginWorkFlow().signIn("admin", "admin")
                        .goToAdministration().goToServerConfigPage()
                        .inputPiwikUrl("http://example.com/piwik")
                        .inputPiwikID("12345").save().goToServerConfigPage();
        assertThat(serverConfigurationPage.getPiwikUrl())
                .isEqualTo("http://example.com/piwik")
                .as("Piwik url is correct is correct");
        assertThat(serverConfigurationPage.getPiwikID()).isEqualTo("12345")
                .as("Piwik ID is correct");
    }
}
