package org.zanata.email

import javax.annotation.Resource
import javax.enterprise.context.ApplicationScoped
import javax.mail.Session

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
internal class MailSessionProducer {
    companion object {
        private const val MAIL_SESSION_JNDI = "mail/Default"
    }

    @field:Resource(lookup = MAIL_SESSION_JNDI)
    private lateinit var session: Session
}
