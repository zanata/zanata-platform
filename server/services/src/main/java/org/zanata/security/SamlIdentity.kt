/*
R * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.security

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.slf4j.LoggerFactory
import org.zanata.dao.CredentialsDAO
import org.zanata.events.AlreadyLoggedInEvent
import org.zanata.exception.NotLoggedInException
import org.zanata.security.annotations.SAML
import org.zanata.security.annotations.SAMLAttribute
import org.zanata.security.annotations.SAMLAttribute.AttributeName.UID
import org.zanata.security.annotations.SAMLAttribute.AttributeName.CN
import org.zanata.security.annotations.SAMLAttribute.AttributeName.EMAIL
import org.zanata.util.Synchronized
import java.io.Serializable
import java.security.Principal
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Event
import javax.inject.Inject

@SessionScoped
@Synchronized
class SamlIdentity
@Inject constructor(private val identity: ZanataIdentity,
                    @field:SuppressFBWarnings("SE_BAD_FIELD")
                    private val alreadyLoggedInEvent: Event<AlreadyLoggedInEvent>,
                    @SAML private val principal: Principal?,
                    @SAMLAttribute(UID) val uid: String?,
                    @SAMLAttribute(CN) val commonName: String?,
                    @SAMLAttribute(EMAIL) val email: String?,
                    private val credentialsDAO: CredentialsDAO) : Serializable, ExternallyAuthenticatedIdentity {
    val uniqueName: String?
        get() = principal?.name

    override fun authenticate() {
        if (identity.isLoggedIn) {
            alreadyLoggedInEvent.fire(AlreadyLoggedInEvent())
            return
        }
        if (principal == null) throw NotLoggedInException()

        log.info("SAML2 login: username: {}, common name: {}, uuid: {}",
                UID, commonName, uniqueName)
        val credentials = credentialsDAO.findByUser(uniqueName)
        // when sign in with SAML2 the first time, there is no HCredentials or HAccount in database
        val username = credentials?.account?.username ?: uid

        identity.credentials.username = username
        identity.credentials.password = ""
        identity.credentials.authType = AuthenticationType.SAML2
        identity.credentials.isInitialized = true
        identity.isPreAuthenticated = true
    }

    override fun login() {
        if (identity.isLoggedIn) {
            alreadyLoggedInEvent.fire(AlreadyLoggedInEvent())
            return
        }

        if (principal == null) throw NotLoggedInException()
        identity.acceptExternallyAuthenticatedPrincipal(principal)
    }

    companion object {
        private const val serialVersionUID = 5341594999046279309L
        private val log = LoggerFactory.getLogger(SamlIdentity::class.java)

    }
}
