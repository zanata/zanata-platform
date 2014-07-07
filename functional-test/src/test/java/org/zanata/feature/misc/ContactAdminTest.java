package org.zanata.feature.misc;

import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.utility.ContactAdminFormPage;
import org.zanata.page.utility.HelpPage;
import org.zanata.util.AddUsersRule;
import org.zanata.util.Constants;
import org.zanata.util.HasEmailRule;
import org.zanata.util.PropertiesHolder;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ContactAdminTest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();
    @ClassRule
    public static HasEmailRule emailRule = new HasEmailRule();

    @Feature(summary = "The user can contact the site administrator",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181717)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void testContactAdmin() {
        DashboardBasePage dashboard =
                new LoginWorkFlow().signIn("translator", "translator");
        ContactAdminFormPage contactAdminFormPage =
                dashboard.goToHelp().clickContactAdmin();

        HelpPage helpPage = contactAdminFormPage
                .inputSubject("hello admin")
                .inputMessage("I love Zanata")
                .send();

        assertThat(helpPage.expectNotification("Your message has been sent " +
                "to the administrator"))
                .isTrue()
                .as("An email sent notification shows");

        List<WiserMessage> messages = emailRule.getMessages();

        assertThat(messages.size())
                .isEqualTo(1)
                .as("One email was sent");

        WiserMessage wiserMessage = messages.get(0);

        assertThat(wiserMessage.getEnvelopeReceiver())
                .isEqualTo("admin@example.com")
                .as("The email recipient is the administrator");

        String content = HasEmailRule.getEmailContent(wiserMessage);

        assertThat(content)
                .contains("Zanata user 'translator' with id 'translator' " +
                        "has sent the following message:")
                .as("The email header is correct");
        assertThat(content)
                .contains("I love Zanata")
                .as("The message content is correct");
        assertThat(content)
                .contains("You can reply to translator at " +
                        "translator@example.com")
                .as("The email instructions are correct");

        // contains instance url (without last slash)
        String instanceUrl = PropertiesHolder.getProperty(
                Constants.zanataInstance.value()).replaceAll("/$", "");

        assertThat(content)
                .contains(instanceUrl)
                .as("The email origin (server) is correct");
    }

}
