/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

import javax.enterprise.event.Observes;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.zanata.events.AlreadyLoggedInEvent;
import org.zanata.events.LoginFailedEvent;
import org.zanata.events.LoginSuccessfulEvent;
import org.zanata.events.NotLoggedInEvent;
import org.zanata.events.UserCreatedEvent;
import org.zanata.ui.faces.FacesMessages;

import static javax.enterprise.event.Reception.IF_EXISTS;

/**
 * Some of the event observers are migrated from org.jboss.seam.security.FacesSecurityEvents
 */
@WindowScoped
@Slf4j
public class AuthenticationEvents implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private FacesMessages facesMessages;

    public void createSuccessful(@Observes(notifyObserver = IF_EXISTS)
    UserCreatedEvent userCreatedEvent) {
        log.info("Account {} created",
                userCreatedEvent.getUser().getUsername());
    }

    public void loginInSuccessful(
            @Observes(notifyObserver = IF_EXISTS) LoginSuccessfulEvent event) {
        log.debug("Account logged in successfully");
        facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO,
                "org.jboss.seam.loginSuccessful", event.getName());
    }

    public void loginFailed(
            @Observes(notifyObserver = IF_EXISTS) LoginFailedEvent event) {
        log.debug("login failed", event.getException());
        facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR,
                "org.jboss.seam.loginFailed");
    }

    public void notLoggedIn(
            @Observes(notifyObserver = IF_EXISTS) NotLoggedInEvent event) {
        facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO,
                "org.jboss.seam.NotLoggedIn");
    }

    public void alreadyLoggedIn(
            @Observes(notifyObserver = IF_EXISTS) AlreadyLoggedInEvent event) {
        facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_WARN,
                "org.jboss.seam.AlreadyLoggedIn");
    }
}
