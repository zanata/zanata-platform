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
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.deltaspike.core.util.ContextUtils;
import org.zanata.events.AlreadyLoggedInEvent;
import org.zanata.events.LoginFailedEvent;
import org.zanata.events.LoginSuccessfulEvent;
import org.zanata.events.NotLoggedInEvent;
import org.zanata.events.UserCreatedEvent;
import org.zanata.ui.faces.FacesMessages;
// TODO get these event observers working again

/**
 * Some of the event observers are migrated from
 * org.jboss.seam.security.FacesSecurityEvents. Most of these events are fired
 * by ZanataIdentity.
 *
 * @see org.zanata.security.ZanataIdentity
 * @see org.zanata.security.SpNegoIdentity
 * @see org.zanata.exception.handler.AccessDeniedExceptionHandler
 * @see org.zanata.exception.handler.NotLoggedInExceptionHandler
 */
@Dependent
public class AuthenticationEvents implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AuthenticationEvents.class);

    private static final long serialVersionUID = 1L;
    @Inject
    private FacesMessages facesMessages;

    public void createSuccessful(@Observes UserCreatedEvent userCreatedEvent) {
        log.info("Account {} created",
                userCreatedEvent.getUser().getUsername());
    }

    public void loginInSuccessful(@Observes LoginSuccessfulEvent event) {
        if (StringUtils.isNotBlank(event.getName())) {
            log.debug("Account logged in successfully");
            if (ContextUtils.isContextActive(WindowScoped.class)) {
                facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO,
                        "authentication.loginSuccessful", event.getName());
            }
        }
    }

    public void loginFailed(@Observes LoginFailedEvent event) {
        log.debug("login failed", event.getException());
        if (ContextUtils.isContextActive(WindowScoped.class)) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR,
                    "authentication.loginFailed");
        }
    }

    public void notLoggedIn(@Observes NotLoggedInEvent event) {
        if (ContextUtils.isContextActive(WindowScoped.class)) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO,
                    "authentication.notLoggedIn");
        }
    }

    public void alreadyLoggedIn(@Observes AlreadyLoggedInEvent event) {
        if (ContextUtils.isContextActive(WindowScoped.class)) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_WARN,
                    "authentication.alreadyLoggedIn");
        }
    }
}
