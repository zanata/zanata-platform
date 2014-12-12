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
package org.zanata.security;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.security.Configuration;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import org.zanata.events.Logout;
import org.zanata.model.HAccount;
import org.zanata.security.permission.MultiTargetList;
import org.zanata.util.ServiceLocator;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class ZanataIdentity extends Identity {
    private static final Logger log = LoggerFactory.getLogger(
            ZanataIdentity.class);

    public static final String JAAS_DEFAULT = "default";

    private static final long serialVersionUID = -5488977241602567930L;

    private String apiKey;

    private boolean preAuthenticated;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        getCredentials().setPassword(apiKey);
    }

    public boolean isApiRequest() {
        return apiKey != null;
    }

    public static ZanataIdentity instance() {
        if (!Contexts.isSessionContextActive()) {
            throw new IllegalStateException("No active session context");
        }

        ZanataIdentity instance =
                ServiceLocator.instance().getInstance(ZanataIdentity.class);

        if (instance == null) {
            throw new IllegalStateException("No Identity could be created");
        }

        return instance;
    }

    public void checkLoggedIn() {
        if (!isLoggedIn()) {
            throw new NotLoggedInException();
        }
    }

    @Override
    public ZanataCredentials getCredentials() {
        return (ZanataCredentials) super.getCredentials();
    }

    @Observer("org.jboss.seam.preDestroyContext.SESSION")
    public void logout() {
        if (Events.exists() && getCredentials() != null) {
            Events.instance().raiseEvent(Logout.EVENT_NAME,
                    new Logout(getCredentials().getUsername()));
        }
        super.logout();
    }

    @Override
    public boolean hasPermission(Object target, String action) {
        log.trace("ENTER hasPermission({}, {})", target, action);
        boolean result = super.hasPermission(target, action);
        if (result) {
            if (log.isDebugEnabled()) {
                log.debug("ALLOWED hasPermission({}, {}) for user {}",
                        target, action, getAccountUsername());
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("DENIED hasPermission({}, {}) for user {}",
                        target, action, getAccountUsername());
            }
        }
        log.trace("EXIT hasPermission(): {}", result);
        return result;
    }

    @Override
    public boolean hasPermission(String name, String action, Object... arg) {
        if (log.isTraceEnabled()) {
            log.trace("ENTER hasPermission({})",
                    Lists.newArrayList(name, action, arg));
        }
        boolean result = super.hasPermission(name, action, arg);
        if (result) {
            if (log.isDebugEnabled()) {
                log.debug("ALLOWED hasPermission({}, {}, {}) for user {}",
                        name, action, arg, getAccountUsername());
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("DENIED hasPermission({}, {}, {}) for user {}",
                        name, action, arg, getAccountUsername());
            }
        }
        log.trace("EXIT hasPermission(): {}", result);
        return result;
    }

    /**
     * Indicates if the user has permission to perform an action on a variable
     * number of targets. This is provided as an extension to Seam's single
     * target permission capabilities.
     *
     * @param action
     *            The permission action.
     * @param targets
     *            Targets for permissions.
     */
    public boolean hasPermission(String action, Object... targets) {
        return super
                .hasPermission(MultiTargetList.fromTargets(targets), action);
    }

    /**
     * Checks permissions on a variable number of targets.This is provided as an
     * extension to Seam's single target permission capabilities.
     *
     * @param action
     *            The permission action.
     * @param targets
     *            Targets for permissions.
     * @throws NotLoggedInException
     *             if not authorised and not logged in
     * @throws org.jboss.seam.security.AuthorizationException
     *             if logged in but not authorised
     */
    public void checkPermission(String action, Object... targets) {
        super.checkPermission(MultiTargetList.fromTargets(targets), action);
    }

    @Override
    public LoginContext getLoginContext() throws LoginException {
        if (isApiRequest()) {
            return new LoginContext(JAAS_DEFAULT, getSubject(),
                    getCredentials().createCallbackHandler(),
                    Configuration.instance());
        }
        if (getJaasConfigName() != null
                && !getJaasConfigName().equals(JAAS_DEFAULT)) {
            return new LoginContext(getJaasConfigName(), getSubject(),
                    getCredentials().createCallbackHandler());
        }

        return new LoginContext(JAAS_DEFAULT, getSubject(), getCredentials()
                .createCallbackHandler(), Configuration.instance());
    }

    public boolean isPreAuthenticated() {
        return preAuthenticated;
    }

    public void setPreAuthenticated(boolean var) {
        this.preAuthenticated = var;
    }

    public String login() {
        // Default to internal authentication
        return this.login(AuthenticationType.INTERNAL);
    }

    public String login(AuthenticationType authType) {
        getCredentials().setAuthType(authType);
        String result = super.login();
        if (result != null && result.equals("loggedIn")) {
            this.preAuthenticated = true;
        }
        return result;
    }

    /**
     * Utility method to get the authenticated account username. This differs
     * from {@link org.jboss.seam.security.Credentials#getUsername()} in that
     * this returns the actual account's username, not the user provided one
     * (which for some authentication systems is non-existent).
     *
     * @return The currently authenticated account username, or null if the
     *         session is not authenticated.
     */
    @Nullable
    public String getAccountUsername() {
        HAccount authenticatedAccount =
                ServiceLocator.instance().getInstance(
                        JpaIdentityStore.AUTHENTICATED_USER, HAccount.class);
        if (authenticatedAccount != null) {
            return authenticatedAccount.getUsername();
        }
        return null;
    }
}
