package org.zanata.email

import kotlinx.html.BODY
import org.zanata.i18n.Messages

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
abstract class HtmlEmailStrategy : AbstractEmailStrategy() {
    abstract fun getReceivedReasons(msgs: Messages): List<String>
    abstract val addresses: EmailAddressBlock
    abstract fun bodyProducer(): BODY.(Messages) -> Unit
}
