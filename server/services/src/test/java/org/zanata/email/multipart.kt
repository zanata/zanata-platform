package org.zanata.email

import org.assertj.core.api.Assertions
import javax.mail.Multipart
import javax.mail.internet.MimeMessage

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
internal data class MultipartContents(val text: String, val html: String)

internal fun extractMultipart(message: MimeMessage): MultipartContents {
    val multipart = message.content as Multipart
    // one for html, one for text
    Assertions.assertThat(multipart.count).isEqualTo(2)

    // Text should appear first (because HTML is the preferred format)
    val textPart = multipart.getBodyPart(0)
    Assertions.assertThat(textPart.dataHandler.contentType).isEqualTo(
            "text/plain; charset=UTF-8")

    val htmlPart = multipart.getBodyPart(1)
    Assertions.assertThat(htmlPart.dataHandler.contentType).isEqualTo(
            "text/html; charset=UTF-8")

    return MultipartContents(textPart.content as String, htmlPart.content as String)
}
