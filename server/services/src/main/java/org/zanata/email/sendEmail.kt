package org.zanata.email

import com.google.common.base.Throwables
import org.zanata.log4j.getLogger
import java.net.ConnectException
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Transport

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
private val log = getLogger {}

/**
 * Build message using 'messageBuilder' and send it via JavaMail Transport to
 * 'addresses'.
 */
internal fun sendEmail(messageBuilder: MessageBuilder): Message {
    try {
        val email = messageBuilder.makeMessage()
        logMessage(email)
        Transport.send(email)
        return email
    } catch (e: MessagingException) {
        val rootCause = Throwables.getRootCause(e)
        if (rootCause.javaClass == ConnectException::class.java && rootCause.message == "Connection refused") {
            throw RuntimeException(
                    "The system failed to connect to mail service. Please contact the administrator!",
                    e)
        }
        throw RuntimeException(e)
    }
}

private fun logMessage(msg: Message) {
    try {
        // NB the body may contain more sensitive information
        if (log.isInfoEnabled) {
            log.info(
                    "Sending message with Subject \"{}\" to Recipients {} From {} Reply-To {}",
                    msg.subject, msg.allRecipients, msg.from,
                    msg.replyTo)
        }
    } catch (e: Exception) {
        log.warn("Unable to log MimeMessage", e)
        // but then keep going; we don't want logging problems to
        // break anything here...
    }
}
