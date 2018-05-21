package org.zanata.email

import javax.annotation.Resource
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Produces
import javax.mail.Session

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
internal class MailSessionProducer {
    @field:Resource(lookup = "java:jboss/mail/Default")
    private lateinit var session: Session

    @Produces
    @Dependent
    fun session() = session
}
