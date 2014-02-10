package org.zanata.feature.misc;

import javax.mail.internet.MimeMultipart;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.DetailedTest;
import org.zanata.page.utility.ContactAdminFormPage;
import org.zanata.page.utility.DashboardPage;
import org.zanata.page.utility.HelpPage;
import org.zanata.util.AddUsersRule;
import org.zanata.util.PropertiesHolder;
import org.zanata.workflow.LoginWorkFlow;
import com.google.common.base.Throwables;

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
    private Wiser wiser = new Wiser();

    @Before
    public void setUp() {
        String port = PropertiesHolder.getProperty("smtp.port");
        wiser.setPort(Integer.parseInt(port));
        wiser.start();
    }

    @After
    public void cleanUp() {
        wiser.stop();
    }

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
        assertThat(wiser.getMessages(), Matchers.hasSize(1));
        WiserMessage wiserMessage = wiser.getMessages().get(0);
        assertThat(wiserMessage.getEnvelopeReceiver(),
                Matchers.equalTo("admin@example.com"));

        String content = getEmailContent(wiserMessage);
        assertThat(content, Matchers.containsString("I love Zanata"));
    }

    private static String getEmailContent(WiserMessage wiserMessage) {
        try {
            return ((MimeMultipart) wiserMessage.getMimeMessage().getContent())
                    .getBodyPart(0).getContent().toString();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
