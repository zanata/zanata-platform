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
import org.zanata.events.AlreadyLoggedInEvent
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
                    private val alreadyLoggedInEvent: Event<AlreadyLoggedInEvent>) : Serializable {

    var uniqueNameId: String? = null
    var email: String? = null
        protected set
    var name: String? = null
        protected set

    fun authenticate(uniqueNameId: String, username: String?,
                     email: String?, name: String?) {
        this.uniqueNameId = uniqueNameId
        this.email = email ?: ""
        this.name = name ?: ""

        if (identity.isLoggedIn) {
            alreadyLoggedInEvent.fire(AlreadyLoggedInEvent())
            return
        }

        with(identity) {
            credentials.username = username
            credentials.password = ""
            credentials.authType = AuthenticationType.SSO
            credentials.isInitialized = true
            isPreAuthenticated = true
        }
    }

    fun login(principal: Principal) {
        if (identity.isLoggedIn) {
            alreadyLoggedInEvent.fire(AlreadyLoggedInEvent())
            return
        }

        identity.acceptExternallyAuthenticatedPrincipal(principal)
    }

    companion object {
        private const val serialVersionUID = 5341594999046279309L
    }
}
