package org.zanata.email

import kotlinx.html.BODY
import org.zanata.i18n.Messages

/**
 * Defines a strategy for creating HTML emails in HtmlEmailBuilder
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
abstract class HtmlEmailStrategy : AbstractEmailStrategy() {
    abstract fun getReceivedReasons(msgs: Messages): List<String>
    abstract val addresses: EmailAddressBlock
    abstract fun bodyProducer(generalContext: GeneralEmailContext): BODY.(Messages) -> Unit
}

/**
 * Defines the top-level configuration which affect most or all generated emails (eg in the email footer)
 * @param serverURL the configured URL of Zanata
 * @param fromEmail the configured From email used by Zanata (ignored by email templates which provide a more specific address)
 */
data class GeneralEmailContext(val serverURL: String, val fromEmail: String)
