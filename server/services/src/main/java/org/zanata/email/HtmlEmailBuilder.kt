package org.zanata.email

import com.google.common.annotations.VisibleForTesting
import kotlinx.html.BODY
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import org.zanata.i18n.Messages
import org.zanata.i18n.MessagesFactory
import org.zanata.servlet.annotations.ServerPath
import org.zanata.util.HtmlUtil
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.mail.Message.RecipientType.BCC
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import kotlin.text.Charsets.UTF_8


/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
// TODO serverPath also exists in MergeContext
class HtmlEmailBuilder @Inject constructor(
        @ServerPath val serverPath: String,
        val mailSession: Session,
        messagesFactory: MessagesFactory) {

    companion object {
        private val UTF8 = UTF_8.name()
    }

    private val msgs: Messages = messagesFactory.defaultLocaleMessages

    /**
     * Build message using 'strategy' and send it via JavaMail Transport.
     */
    fun sendMessage(strategy: HtmlEmailStrategy) =
            sendEmail(MessageBuilder {
                buildMessage(MimeMessage(mailSession), strategy.addresses, strategy.getSubject(msgs), strategy.getReceivedReasons(msgs), serverPath, strategy.bodyProducer())
            })

    /**
     * Fills in the provided MimeMessage 'msg' using 'strategy' to select the
     * desired body template and to provide context variable values. Does not
     * actually send the email.
     *
     * @return the same message
     * @throws javax.mail.MessagingException
     */
    @VisibleForTesting
    @Throws(MessagingException::class)
    internal fun buildMessage(
            msg: MimeMessage,
            addresses: EmailAddressBlock,
            subject: String,
            receivedReasons: List<String>,
            serverPath: String,
            bodyCallback : BODY.(Messages) -> Unit): MimeMessage {
        // TODO remember users' locales, and customise for each recipient
        // msgs = messagesFactory.getMessages(account.getLocale());
        msg.setFrom(addresses.fromAddress)
        if (addresses.replyToAddress != null) {
            msg.replyTo = addresses.replyToAddress.toTypedArray()
        }
        msg.addRecipients(BCC, addresses.toAddresses.toTypedArray())
        msg.setSubject(subject, UTF8)
        // optional future extension
        // strategy.setMailHeaders(msg, msgs)
        val body = buildBodyWithFooter(msgs, serverPath, receivedReasons, bodyCallback)
        // Alternative parts should be added in increasing order of preference,
        // ie the preferred format should be added last.
        val mp = MimeMultipart("alternative")
        val textPart = MimeBodyPart()
        val text = HtmlUtil.htmlToText(body)
        textPart.setText(text, "UTF-8")
        mp.addBodyPart(textPart)
        val htmlPart = MimeBodyPart()
        htmlPart.setContent(body, "text/html; charset=UTF-8")
        mp.addBodyPart(htmlPart)
        msg.setContent(mp)
        return msg
    }

    /**
     * Builds the complete HTML email body (including html, head and body elements). bodyCallback will be
     * invoked inside the body element to supply the main HTML body (which will be followed by the
     * generic footer).
     */
    private fun buildBodyWithFooter(msgs: Messages, serverPath: String = "", receivedReasons: List<String> = listOf(), bodyCallback : BODY.(Messages) -> Unit): String {
        return StringBuilder().appendHTML().html {
            head {
                meta(charset = UTF8)
            }
            body {
                bodyCallback(msgs)
                hr()

                if (!receivedReasons.isEmpty()) {
                    p {
                        span {
                            +msgs["jsf.email.YouAreReceivingThisMailBecause"]!!
                            br()
                            receivedReasons.forEach {
                                +it
                                br()
                            }
                        }
                    }
                }
                p {
                    +msgs["jsf.email.GeneratedFromZanataServerAt"]!!
                    +" "
                    a(href = serverPath) {
                        +serverPath
                    }
                }
            }
        }.toString()
    }
}

data class EmailAddressBlock @JvmOverloads constructor(
        val fromAddress: InternetAddress,
        val toAddresses: List<InternetAddress>,
        val replyToAddress: List<InternetAddress>? = null)
