/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.security;

import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import javax.inject.Named;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.zanata.events.AlreadyLoggedInEvent;
import org.zanata.events.LoginFailedEvent;
import org.zanata.events.LoginSuccessfulEvent;
import org.zanata.events.NotLoggedInEvent;

import static org.jboss.seam.annotations.Install.APPLICATION;

/**
 *
 * TODO [CDI] use FacesMessage.addGlobal to achieve the same effect.
 *
 * what it does is add them to the global messages list, Seam's FacesMessages
 * extends StatusMessages, and it displays any waiting global messages in
 * beforeRenderResponse(). So FacesSecurityEvents could generate the status
 * messages and call our FacesMessages.addGlobal to achieve the same effect.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("org.jboss.seam.security.facesSecurityEvents")
@javax.enterprise.context.ApplicationScoped
@Install(precedence = APPLICATION,
        classDependencies = "javax.faces.context.FacesContext")
@BypassInterceptors
/* TODO [CDI] Remove @PostConstruct from startup method and make it accept (@Observes @Initialized ServletContext context) */
public class FacesSecurityEvents {

    @Observer(LoginFailedEvent.EVENT_NAME)
    public void addLoginFailedMessage(LoginFailedEvent loginFailedEvent) {
        StatusMessages.instance().addFromResourceBundleOrDefault(
                StatusMessage.Severity.ERROR,
                "org.jboss.seam.loginFailed",
                "Login failed",
                loginFailedEvent.getException());
    }

    @Observer(LoginSuccessfulEvent.EVENT_NAME)
    public void addLoginSuccessfulMessage() {
        StatusMessages.instance().addFromResourceBundleOrDefault(
                StatusMessage.Severity.INFO,
                "org.jboss.seam.loginSuccessful",
                "Welcome, #0",
                ZanataIdentity.instance().getCredentials().getUsername());
    }

    @Observer(NotLoggedInEvent.EVENT_NAME)
    public void addNotLoggedInMessage() {
        StatusMessages.instance().addFromResourceBundleOrDefault(
                StatusMessage.Severity.WARN,
                "org.jboss.seam.NotLoggedIn",
                "Please log in first"
                );
    }

    @Observer(AlreadyLoggedInEvent.EVENT_NAME)
    public void addAlreadyLoggedInMessage() {
        StatusMessages.instance().addFromResourceBundleOrDefault(
                StatusMessage.Severity.WARN,
                "org.jboss.seam.AlreadyLoggedIn",
                "You are already logged in, please log out first if you wish to log in again"
                );
    }
}
