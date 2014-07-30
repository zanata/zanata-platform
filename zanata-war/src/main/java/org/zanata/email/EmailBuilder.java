package org.zanata.email;

import static com.google.common.base.Charsets.UTF_8;
import static javax.mail.Message.RecipientType.TO;

import java.io.StringWriter;
import java.net.ConnectException;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.common.base.Throwables;
import com.googlecode.totallylazy.collections.PersistentMap;
import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.mail.MailSession;
import org.zanata.ApplicationConfiguration;
import org.zanata.i18n.Messages;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import static com.googlecode.totallylazy.collections.PersistentMap.constructors.map;
import static org.jboss.seam.ScopeType.EVENT;
import static org.jboss.seam.ScopeType.STATELESS;

/**
 * Uses an instance of EmailBuilderStrategy to build an email from a Velocity
 * template and send it via the default JavaMail Transport.
 */
@AllArgsConstructor
@AutoCreate
@Name("emailBuilder")
@Scope(STATELESS)
@Slf4j
public class EmailBuilder {
    // Use this if you want emails logged on stderr
    // Warning: The full message may contain sensitive information
    private static final boolean LOG_FULL_MESSAGES = false;
    private static final VelocityEngine velocityEngine = makeVelocityEngine();

    public EmailBuilder() {
        this.mailSession = MailSession.instance();
    }

    // it seems to be impossible to inject this in Seam 2:
    private final Session mailSession;
    @In
    private Context emailContext;
    @In
    private Messages msgs;

    private static VelocityEngine makeVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        // this allows unit tests to detect missing context vars:
        ve.setProperty("runtime.references.strict", true);
        ve.init();
        return ve;
    }

    /**
     * Build message using 'strategy' and send it via Transport to 'toAddress'.
     * @param strategy
     * @throws javax.mail.MessagingException
     */
    public void sendMessage(EmailStrategy strategy,
            String receivedReason, InternetAddress toAddress) {
        sendMessage(strategy, receivedReason, new InternetAddress[] {
                        toAddress });
    }

    /**
     * Build message using 'strategy' and send it via Transport to 'toAddresses'.
     * @param strategy
     * @throws javax.mail.MessagingException
     */
    public void sendMessage(EmailStrategy strategy,
            String receivedReason, InternetAddress[] toAddresses) {
        try {
            MimeMessage email = new MimeMessage(mailSession);
            buildMessage(email, strategy, toAddresses, receivedReason);
            logMessage(email);
            Transport.send(email);
        } catch (MessagingException e) {
            Throwable rootCause = Throwables.getRootCause(e);
            if (rootCause.getClass().equals(ConnectException.class) && rootCause.getMessage().equals("Connection refused")) {
                throw new RuntimeException("The system failed to connect to mail service. Please contact the administrator!");
            }
            throw new RuntimeException(e);
        }
    }

    private void logMessage(MimeMessage msg) {
        try {
            // NB the body may contain more sensitive information
            if (log.isInfoEnabled()) {
                log.info(
                        "Sending message with Subject \"{}\" to Recipients {} From {} Reply-To {}",
                        msg.getSubject(), msg.getAllRecipients(), msg.getFrom(),
                        msg.getReplyTo());
            }
            // The stderr log is perhaps less likely to be distributed widely
            // than normal logging
            if (LOG_FULL_MESSAGES) {
                msg.writeTo(System.err);
            }
        } catch (Exception e) {
            log.warn("Unable to log MimeMessage", e);
        }
    }

    /**
     * Fills in the provided MimeMessage 'msg' using 'strategy' to select the
     * desired body template and to provide context variable values.  Does not
     * actually send the email.
     * @param msg
     * @param strategy
     * @return
     * @throws javax.mail.MessagingException
     */
    @VisibleForTesting
    MimeMessage buildMessage(MimeMessage msg, EmailStrategy strategy,
            InternetAddress[] toAddresses, String receivedReason)
            throws MessagingException {

        Optional<InternetAddress> from = strategy.getFromAddress();
        msg.setFrom(from.or(Addresses.getAddress(
                emailContext.getFromAddress(), emailContext.getFromName())));
        Optional<InternetAddress[]> replyTo = strategy.getReplyToAddress();
        if (replyTo.isPresent()) {
            msg.setReplyTo(replyTo.get());
        }
        msg.addRecipients(TO, toAddresses);
        msg.setSubject(strategy.getSubject(msgs), UTF_8.name());
        // optional future extension
//        strategy.setMailHeaders(msg, msgs);

        PersistentMap<String, Object> genericContext = map(
                "msgs",  msgs,
                "receivedReason", receivedReason,
                "serverPath", emailContext.getServerPath());

        // the Map needs to be mutable for "foreach" to work
        VelocityContext context = new VelocityContext(
                strategy.makeContext(genericContext, toAddresses).toMutableMap());
        Template template =
                velocityEngine.getTemplate(strategy.getTemplateResourceName());
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        String body = writer.toString();

        // Alternative parts should be added in increasing order of preference,
        // ie the preferred format should be added last.
        Multipart mp = new MimeMultipart("alternative");

        MimeBodyPart textPart = new MimeBodyPart();
        String text = EmailUtil.htmlToText(body);
        textPart.setText(text, "UTF-8");
        mp.addBodyPart(textPart);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(body, "text/html; charset=UTF-8");
        mp.addBodyPart(htmlPart);

        msg.setContent(mp);
        return msg;
    }

    /**
     * A Seam component which can inject the required configuration and
     * components needed to create EmailBuilder at runtime.
     */
    @AutoCreate
    @Name("emailContext")
    @Scope(EVENT)
    public static class Context {
        @In
        private ApplicationConfiguration applicationConfiguration;
        @In
        private Messages msgs;
        String getServerPath() {
            return applicationConfiguration.getServerPath();
        }
        String getFromAddress() {
            return applicationConfiguration.getFromEmailAddr();
        }
        String getFromName() {
            return msgs.get("jsf.Zanata");
        }
    }

}
