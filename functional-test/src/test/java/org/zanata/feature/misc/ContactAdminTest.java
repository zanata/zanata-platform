package org.zanata.feature.misc;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.DetailedTest;
import org.zanata.page.utility.ContactAdminFormPage;
import org.zanata.page.utility.DashboardPage;
import org.zanata.page.utility.HelpPage;
import org.zanata.util.AddUsersRule;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * TCMS test case <a
 * href="https://tcms.engineering.redhat.com/case/181717">181717</a>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ContactAdminTest {
    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();
    @ClassRule
    public static HasEmailRule emailRule = new HasEmailRule();

    @Test
    public void testContactAdmin() {
        DashboardPage dashboard =
                new LoginWorkFlow().signIn("translator", "translator");
        ContactAdminFormPage contactAdminFormPage =
                dashboard.goToHelp().clickContactAdmin();

        HelpPage helpPage =
                contactAdminFormPage.inputSubject("hello admin")
                        .inputMessage("I love Zanata").send();

        assertThat(
                helpPage.getNotificationMessage(),
                Matchers.equalTo("Your message has been sent to the administrator"));
        List<WiserMessage> messages = emailRule.getMessages();
        assertThat(messages, Matchers.hasSize(1));
        WiserMessage wiserMessage = messages.get(0);
        assertThat(wiserMessage.getEnvelopeReceiver(),
                Matchers.equalTo("admin@example.com"));

        String content = HasEmailRule.getEmailContent(wiserMessage);
        assertThat(content, Matchers.containsString("I love Zanata"));
    }

}
