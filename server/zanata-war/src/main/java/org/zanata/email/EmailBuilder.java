package org.zanata.email;

import static com.google.common.base.Charsets.UTF_8;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.TO;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.List;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.google.common.base.Throwables;
import javaslang.collection.HashMap;
import javaslang.collection.Map;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.ApplicationConfiguration;
import org.zanata.i18n.Messages;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import org.zanata.i18n.MessagesFactory;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.util.HtmlUtil;

/**
 * Uses an instance of EmailBuilderStrategy to build an email from a Velocity
 * template and send it via the default JavaMail Transport.
 */
@Named("emailBuilder")
@javax.enterprise.context.Dependent
public class EmailBuilder {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EmailBuilder.class);
    public static final String MAIL_SESSION_JNDI = "mail/Default";
    // Use this if you want emails logged on stderr
    // Warning: The full message may contain sensitive information
    private static final boolean LOG_FULL_MESSAGES = false;
    private static final VelocityEngine velocityEngine = makeVelocityEngine();

    public EmailBuilder() {
    }

    @Resource(name = MAIL_SESSION_JNDI)
    private Session mailSession;
    @Inject
    private Context emailContext;
    @Inject
    private MessagesFactory messagesFactory;

    private static VelocityEngine makeVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        // send Velocity log to SLF4J (via Commons Logging)
        ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
                CommonsLogLogChute.class.getName());
        // this allows unit tests to detect missing context vars:
        ve.setProperty("runtime.references.strict", true);
        ve.init();
        return ve;
    }

    /**
     * Build message using 'strategy' and send it via Transport to 'toAddress'.
     *
     * @param strategy
     * @throws javax.mail.MessagingException
     */
    public void sendMessage(EmailStrategy strategy,
            List<String> receivedReasons, InternetAddress toAddress) {
        sendMessage(strategy, receivedReasons,
                new InternetAddress[] { toAddress });
    }

    /**
     * Build message using 'strategy' and send it via Transport to
     * 'toAddresses'.
     *
     * @param strategy
     * @throws javax.mail.MessagingException
     */
    public void sendMessage(EmailStrategy strategy,
            List<String> receivedReasons, InternetAddress[] toAddresses) {
        try {
            MimeMessage email = new MimeMessage(mailSession);
            buildMessage(email, strategy, toAddresses, receivedReasons);
            logMessage(email);
            Transport.send(email);
        } catch (MessagingException e) {
            Throwable rootCause = Throwables.getRootCause(e);
            if (rootCause.getClass().equals(ConnectException.class)
                    && rootCause.getMessage().equals("Connection refused")) {
                throw new RuntimeException(
                        "The system failed to connect to mail service. Please contact the administrator!",
                        e);
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
     * desired body template and to provide context variable values. Does not
     * actually send the email.
     *
     * @param msg
     * @param strategy
     * @return
     * @throws javax.mail.MessagingException
     */
    @VisibleForTesting
    MimeMessage buildMessage(MimeMessage msg, EmailStrategy strategy,
            InternetAddress[] toAddresses, List<String> receivedReasons)
            throws MessagingException {
        // TODO remember users' locales, and customise for each recipient
        // msgs = messagesFactory.getMessages(account.getLocale());
        Messages msgs = messagesFactory.getDefaultLocaleMessages();
        Optional<InternetAddress> from = strategy.getFromAddress();
        String fromName = msgs.get("jsf.Zanata");
        msg.setFrom(from.or(
                Addresses.getAddress(emailContext.getFromAddress(), fromName)));
        Optional<InternetAddress[]> replyTo = strategy.getReplyToAddress();
        if (replyTo.isPresent()) {
            msg.setReplyTo(replyTo.get());
        }
        msg.addRecipients(BCC, toAddresses);
        msg.setSubject(strategy.getSubject(msgs), UTF_8.name());
        // optional future extension
        // strategy.setMailHeaders(msg, msgs);
        Map<String, Object> genericContext =
                HashMap.of("msgs", msgs, "receivedReasons", receivedReasons,
                        "serverPath", emailContext.getServerPath());
        // the Map needs to be mutable for "foreach" to work
        VelocityContext context = new VelocityContext(
                strategy.makeContext(genericContext, toAddresses).toJavaMap());
        Template template =
                velocityEngine.getTemplate(strategy.getTemplateResourceName());
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        String body = writer.toString();
        // Alternative parts should be added in increasing order of preference,
        // ie the preferred format should be added last.
        Multipart mp = new MimeMultipart("alternative");
        MimeBodyPart textPart = new MimeBodyPart();
        String text = HtmlUtil.htmlToText(body);
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
    @Named("emailContext")
    @javax.enterprise.context.RequestScoped
    public static class Context {

        @Inject
        @ServerPath
        String serverPath;
        @Inject
        private ApplicationConfiguration applicationConfiguration;

        String getServerPath() {
            return this.serverPath;
        }

        String getFromAddress() {
            return applicationConfiguration.getFromEmailAddr();
        }
    }

    @java.beans.ConstructorProperties({ "mailSession", "emailContext",
            "messagesFactory" })
    public EmailBuilder(final Session mailSession, final Context emailContext,
            final MessagesFactory messagesFactory) {
        this.mailSession = mailSession;
        this.emailContext = emailContext;
        this.messagesFactory = messagesFactory;
    }
}
