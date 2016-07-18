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

import org.apache.deltaspike.core.api.literal.DeltaSpikeLiteral;
import org.jboss.security.SecurityContextAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.events.AlreadyLoggedInEvent;
import org.zanata.util.ServiceLocator;
import org.zanata.util.Synchronized;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@SessionScoped
@Synchronized
public class SpNegoIdentity implements Serializable {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpNegoIdentity.class);
    private static final long serialVersionUID = 5341594999046279309L;

    @Inject
    private ZanataIdentity identity;

    @Inject
    private Event<AlreadyLoggedInEvent> alreadyLoggedInEventEvent;

    public void authenticate() {
        ZanataIdentity identity =
                ServiceLocator.instance().getInstance(ZanataIdentity.class);
        if (identity.isLoggedIn()) {
            getAlreadyLoggedInEvent().fire(new AlreadyLoggedInEvent());
            return;
        }

        HttpServletRequest servletRequest = ServiceLocator.instance()
                .getInstance(HttpServletRequest.class, new DeltaSpikeLiteral());
        String username =
                servletRequest.getUserPrincipal().getName();
        // Remove the domain name, if there is one
        if (username.indexOf('@') > 0) {
            username = username.substring(0, username.indexOf('@'));
        }
        LOGGER.debug("remote username: {}", username);

        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword("");
        identity.getCredentials().setAuthType(AuthenticationType.KERBEROS);
        identity.getCredentials().setInitialized(true);
        identity.setPreAuthenticated(true);
    }

    private Event<AlreadyLoggedInEvent> getAlreadyLoggedInEvent() {
        return alreadyLoggedInEventEvent;
    }

    public void login() {
        if (identity.isLoggedIn()) {
            getAlreadyLoggedInEvent().fire(new AlreadyLoggedInEvent());
            return;
        }

        identity.acceptExternalSubjectAndPpal(
                SecurityContextAssociation.getSubject(),
                SecurityContextAssociation.getPrincipal());
    }
}
