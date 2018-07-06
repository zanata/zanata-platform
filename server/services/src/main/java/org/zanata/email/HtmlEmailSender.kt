package org.zanata.email

import org.zanata.config.ServerFromEmail
import org.zanata.i18n.MessagesFactory
import org.zanata.servlet.annotations.ServerPath
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.MimeMessage

/**
 * This class builds email messages using a kotlinx.html builder, controlled by an HtmlEmailStrategy,
 * and sends the resulting MimeMessage via JavaMail.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
class HtmlEmailSender @Inject constructor(
        @ServerPath val serverPath: String,
        @ServerFromEmail val serverFromEmail: String,
        val mailSession: Session,
        val messagesFactory: MessagesFactory) {
    /**
     * Build message using 'strategy' and send it via JavaMail Transport.
     */
    fun sendMessage(strategy: HtmlEmailStrategy): Message {
        val generalContext = GeneralEmailContext(serverPath, serverFromEmail)
        return sendEmail(MessageBuilder {
            buildMessage(
                    MimeMessage(mailSession),
                    strategy,
                    generalContext,
                    messagesFactory)
        })
    }
}
