package org.zanata.test

import org.apache.commons.io.output.CountingOutputStream
import org.apache.commons.io.output.NullOutputStream
import org.slf4j.LoggerFactory
import java.io.ObjectOutputStream
import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@WebListener
class SessionChecker : HttpSessionListener {
    private val log = LoggerFactory.getLogger(SessionChecker::class.java)

    override fun sessionCreated(event: HttpSessionEvent) {}
    override fun sessionDestroyed(event: HttpSessionEvent) = checkSerializability(event.session)
    private fun checkSerializability(session: HttpSession) {
        log.trace("Checking serializability for session '{}'", session.id)
        try {
            var totalBytes = 0L
            val counter = CountingOutputStream(NullOutputStream())
            ObjectOutputStream(counter).use { oos ->
                for (name in session.attributeNames.iterator()) {
                    counter.resetByteCount()
                    val attrValue = session.getAttribute(name)
                    oos.writeObject(attrValue)
                    oos.flush()
                    val size = counter.byteCount
                    totalBytes += size
                    val className = attrValue::class.qualifiedName
                    if (size > 50_000) {
                        log.warn("Large attribute '{}' of type '{}': {} bytes, value: {}", name, className, size, attrValue)
                    }
                    log.debug("Session attribute '{}' of type '{}': {} bytes, value: {}", name, className, size, attrValue)
                }
                log.debug("Size of session '{}': {} bytes", session.id, totalBytes)
            }
        } catch (e: Exception) {
            log.error("Unable to serialize session attributes", e)
        }
    }
}
