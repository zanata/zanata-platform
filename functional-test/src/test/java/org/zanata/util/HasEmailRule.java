package org.zanata.util;

import java.util.List;

import javax.mail.internet.MimeMultipart;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class HasEmailRule implements TestRule {
    private final Wiser wiser = new Wiser();

    public HasEmailRule() {
        String port = PropertiesHolder.getProperty("smtp.port");
        wiser.setPort(Integer.parseInt(port));
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                wiser.start();
                try {
                    base.evaluate();
                } finally {
                    wiser.getMessages().clear();
                    wiser.stop();
                }
            }
        };
    }

    public List<WiserMessage> getMessages() {
        return wiser.getMessages();
    }

    public static String getEmailContent(WiserMessage wiserMessage) {
        try {
            return ((MimeMultipart) wiserMessage.getMimeMessage().getContent())
                    .getBodyPart(0).getContent().toString();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
