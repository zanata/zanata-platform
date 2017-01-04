/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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

import javax.enterprise.context.ApplicationScoped;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.jsf.api.listener.phase.JsfPhaseListener;
import org.zanata.ApplicationConfiguration;
import org.zanata.config.AllowAnonymousAccess;
import org.zanata.exception.NotLoggedInException;
import org.zanata.util.FacesNavigationUtil;

/**
 * This JSF phase listener will check permissions if the anonymous user is not
 * allowed to view resources.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
@JsfPhaseListener
public class AnonymousAccessControlPhaseListener implements PhaseListener {
    private Provider<Boolean> allowAnonymousAccessProvider;
    private ZanataIdentity identity;

    private HttpServletRequest request;

    @Inject
    public AnonymousAccessControlPhaseListener(
            @AllowAnonymousAccess
                    Provider<Boolean> allowAnonymousAccessProvider,
            ZanataIdentity identity, @DeltaSpike HttpServletRequest request) {
        this.allowAnonymousAccessProvider = allowAnonymousAccessProvider;
        this.identity = identity;
        this.request = request;
    }

    @SuppressWarnings("unused")
    AnonymousAccessControlPhaseListener() {
    }

    @Override
    public void afterPhase(PhaseEvent phaseEvent) {
    }

    @Override
    public void beforePhase(PhaseEvent phaseEvent) {
        if (!requestingPageIsSignInOrRegister() &&
                anonymousAccessIsNotAllowed()) {
            throw new NotLoggedInException();
        }
    }

    private boolean anonymousAccessIsNotAllowed() {
        return !allowAnonymousAccessProvider.get() && !identity.isLoggedIn();
    }

    private boolean requestingPageIsSignInOrRegister() {
        // the request URI will be the internal URI
        String contextPath = request.getContextPath();
        return request.getRequestURI().startsWith(contextPath + "/account/") ||
                request.getRequestURI().startsWith(contextPath + "/public/");
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}
