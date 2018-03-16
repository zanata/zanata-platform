package org.zanata.test

import org.apache.deltaspike.core.api.lifecycle.Destroyed
import org.apache.deltaspike.core.api.lifecycle.Initialized
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.servlet.http.HttpSession

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
class SessionTracker {
    private val log = LoggerFactory.getLogger(SessionTracker::class.java)
    private val sessions = mutableSetOf<HttpSession>()

    @Synchronized
    internal fun sessionCreated(@Observes @Initialized session: HttpSession) {
        log.debug("creating session: {}", session)
        sessions.add(session)
    }

    @Synchronized
    internal fun sessionDestroyed(@Observes @Destroyed session: HttpSession) {
        log.debug("removing session: {}", session)
        sessions.remove(session)
    }

    @Synchronized
    fun invalidateAllSessions() {
        // take copy to avoid ConcurrentModificationException
        val list = sessions.toList()
        if (list.isNotEmpty()) {
            log.info("invalidating {} session(s)", list.size)
            list.forEach(HttpSession::invalidate)
        }
    }

}
