/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.exception.handler;

import java.util.Set;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.zanata.events.NotLoggedInEvent;
import org.zanata.security.CheckRoleDecisionVoter;

/**
 * Handles deltaspike security interceptor exception
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @see org.zanata.security.CheckLoggedInProvider
 * @see org.zanata.security.CheckRoleDecisionVoter
 */
@ExceptionHandler
public class AccessDeniedExceptionHandler extends AbstractExceptionHandler {
    @Inject
    private Event<NotLoggedInEvent> notLoggedInEventEvent;

    public void handleException(
            @Handles ExceptionEvent<AccessDeniedException> event) {
        AccessDeniedException accessDeniedException = event.getException();
        Set<SecurityViolation> violations =
                accessDeniedException.getViolations();
        boolean checkRoleFailed = violations.stream()
                .anyMatch(
                        (violation) -> violation instanceof CheckRoleDecisionVoter.CheckRoleSecurityViolation);
        if (checkRoleFailed) {

            // SecurityViolation type is CheckRoleSecurityViolation
            // we handle them as AuthorizationExceptionHandler
            handle(event, LogLevel.Debug, FacesMessage.SEVERITY_ERROR,
                    "jsf.YouDoNotHavePermissionToAccessThisResource");
        } else {
            // otherwise, we handle it as NotLoggedInExceptionHandler (for now this is the only other type of violation)
            notLoggedInEventEvent.fire(new NotLoggedInEvent());
            handle(event, LogLevel.Debug, urlUtil.signInPage(),
                    FacesMessage.SEVERITY_WARN,
                    "authentication.notLoggedIn");
        }
    }
}
