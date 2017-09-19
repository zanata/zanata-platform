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
package org.zanata.security;

import java.io.Serializable;
import java.security.Principal;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.zanata.events.AlreadyLoggedInEvent;
import org.zanata.util.ServiceLocator;
import org.zanata.util.Synchronized;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SessionScoped
@Synchronized
public class SamlIdentity implements Serializable {

    private static final long serialVersionUID = 5341594999046279309L;

    @Inject
    private ZanataIdentity identity;

    @SuppressFBWarnings("SE_BAD_FIELD")
    @Inject
    private Event<AlreadyLoggedInEvent> alreadyLoggedInEventEvent;
    private String uniqueNameId;
    private String email;
    private String name;

    public void authenticate(String uniqueNameId, String username,
            String email, String name) {
        this.uniqueNameId = uniqueNameId;
        this.email = email;
        this.name = name;
        ZanataIdentity identity =
                ServiceLocator.instance().getInstance(ZanataIdentity.class);
        if (identity.isLoggedIn()) {
            getAlreadyLoggedInEvent().fire(new AlreadyLoggedInEvent());
            return;
        }

        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword("");
        identity.getCredentials().setAuthType(AuthenticationType.SSO);
        identity.getCredentials().setInitialized(true);
        identity.setPreAuthenticated(true);
    }

    private Event<AlreadyLoggedInEvent> getAlreadyLoggedInEvent() {
        return alreadyLoggedInEventEvent;
    }

    public String getUniqueNameId() {
        return uniqueNameId;
    }

    public String getEmail() {
        return email;
    }

    public void login(Principal principal) {
        if (identity.isLoggedIn()) {
            getAlreadyLoggedInEvent().fire(new AlreadyLoggedInEvent());
            return;
        }

        identity.acceptExternallyAuthenticatedPrincipal(principal);
    }

    public String getName() {
        return name;
    }
}
