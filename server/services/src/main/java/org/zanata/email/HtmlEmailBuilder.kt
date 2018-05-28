@file:Suppress("MatchingDeclarationName")
package org.zanata.email

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.zanata.i18n.Messages
import org.zanata.i18n.MessagesFactory
import org.zanata.util.HtmlUtil
import javax.mail.Message.RecipientType.BCC
import javax.mail.MessagingException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

private const val UTF8 = "UTF-8"

/**
 * This function builds email messages using a kotlinx.html builder, controlled by an HtmlEmailStrategy.
 * Fills in the provided MimeMessage 'msg' using 'strategy' to select the
 * desired body template and to provide context variable values. Does not
 * actually send the email.
 *
 * @return the same message
 * @throws javax.mail.MessagingException
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Throws(MessagingException::class)
internal fun buildMessage(
        msg: MimeMessage,
        strategy: HtmlEmailStrategy,
        generalContext: GeneralEmailContext,
        messagesFactory: MessagesFactory): MimeMessage {

    // TODO remember users' locales, and customise reasons/body/footer/fromName for each recipient
    // msgs = messagesFactory.getMessages(account.getLocale());
    val msgs: Messages = messagesFactory.defaultLocaleMessages

    val addresses = strategy.addresses
    val subject = strategy.getSubject(msgs)
    val receivedReasons = strategy.getReceivedReasons(msgs)
    val bodyCallback = strategy.bodyProducer(generalContext)

    if (addresses.fromAddress != null) {
        msg.setFrom(addresses.fromAddress)
    } else {
        val fromName = msgs["jsf.Zanata"]
        msg.setFrom(Addresses.getAddress(generalContext.fromEmail, fromName))
    }

    if (addresses.replyToAddress != null) {
        msg.replyTo = addresses.replyToAddress.toTypedArray()
    }
    msg.addRecipients(BCC, addresses.toAddresses.toTypedArray())
    msg.setSubject(subject, UTF8)
    // optional future extension
    // strategy.setMailHeaders(msg, msgs)
    val body = buildBodyWithFooter(msgs, generalContext.serverURL, receivedReasons, bodyCallback)
    // Alternative parts should be added in increasing order of preference,
    // ie the preferred format should be added last.
    val multipart = MimeMultipart("alternative")
    val textPart = MimeBodyPart()
    val text = HtmlUtil.htmlToText(body)
    textPart.setText(text, "UTF-8")
    multipart.addBodyPart(textPart)
    val htmlPart = MimeBodyPart()
    htmlPart.setContent(body, "text/html; charset=UTF-8")
    multipart.addBodyPart(htmlPart)
    msg.setContent(multipart)
    return msg
}

/*
 * Builds the complete HTML email body (including html, head and body elements). bodyCallback will be
 * invoked inside the body element to supply the main HTML body (which will be followed by the
 * generic footer).
 */
private fun buildBodyWithFooter(
        msgs: Messages,
        serverPath: String = "",
        receivedReasons: List<String> = listOf(),
        bodyCallback: BODY.(Messages) -> Unit): String {
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

/**
 * Holds the email addresses (From, To, Reply-To) for an email to be sent
 */
data class EmailAddressBlock @JvmOverloads constructor(
        /**
         * The "From" address which should be used for this email (optional) -
         * if absent, the server's configured "From" email will be used
         */
        val fromAddress: InternetAddress? = null,
        /**
         * The "To" address(es) which should be used for this email
         */
        val toAddresses: List<InternetAddress>,
        /**
         * The Reply-To address(es) which should be used for this email (optional)
         */
        val replyToAddress: List<InternetAddress>? = null)
