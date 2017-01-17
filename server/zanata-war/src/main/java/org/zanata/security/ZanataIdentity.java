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

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.auth.Subject;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpSession;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.dao.AccountDAO;
import org.zanata.events.AlreadyLoggedInEvent;
import org.zanata.events.LoginFailedEvent;
import org.zanata.events.LoginSuccessfulEvent;
import org.zanata.events.LogoutEvent;
import org.zanata.events.NotLoggedInEvent;
import org.zanata.exception.AuthorizationException;
import org.zanata.exception.NotLoggedInException;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.HasUserFriendlyToString;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.security.jaas.InternalLoginModule;
import org.zanata.security.permission.CustomPermissionResolver;
import org.zanata.security.permission.MultiTargetList;
import org.zanata.servlet.annotations.SessionId;
import org.zanata.util.Contexts;
import org.zanata.util.RequestContextValueStore;
import org.zanata.util.ServiceLocator;
import org.zanata.util.Synchronized;
import org.zanata.util.UrlUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

@Named("identity")
@SessionScoped
@Synchronized
public class ZanataIdentity implements Identity, Serializable {
    private static final Logger log = LoggerFactory.getLogger(
            ZanataIdentity.class);

    public static final String JAAS_DEFAULT = "default";
    public static final String ROLES_GROUP = "Roles";

    private static final long serialVersionUID = -5488977241602567930L;

    protected static boolean securityEnabled = true;
    private static final String LOGIN_TRIED = "security.loginTried";
    private static final String SILENT_LOGIN = "security.silentLogin";

    private transient ThreadLocal<Boolean> systemOp;

    private String apiKey;

    private boolean preAuthenticated;
    private Subject subject = new Subject();
    private Principal principal;
    private List<String> preAuthenticationRoles = new ArrayList<>();
    @Inject
    private CustomPermissionResolver permissionResolver;
    @Inject
    private ZanataCredentials credentials;
    private boolean authenticating;
    private String jaasConfigName = "zanata";

    @Inject
    private Event<LoginSuccessfulEvent> loginSuccessfulEventEvent;

    @Inject
    private Event<LogoutEvent> logoutEvent;

    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "CDI proxies are Serializable")
    private RequestContextValueStore requestContextValueStore;

    @Inject
    private UrlUtil urlUtil;

    @Inject
    private Event<AlreadyLoggedInEvent> alreadyLoggedInEventEvent;

    @Inject
    private Event<LoginFailedEvent> loginFailedEventEvent;

    @Inject
    private Event<NotLoggedInEvent> notLoggedInEventEvent;

    @Inject
    private AccountDAO accountDAO;

    @Inject @SessionId
    private String sessionId;

    // The following fields store user details for use during preDestroy().
    // NB: they are not currently kept up to date if the user name changes.
    private @Nullable String cachedPersonEmail;
    private @Nullable String cachedPersonName;
    private @Nullable String cachedUsername;
    private boolean requestUsingOAuth;

    public static boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public static void setSecurityEnabled(boolean securityEnabled) {
        ZanataIdentity.securityEnabled = securityEnabled;
    }

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

    public static ZanataIdentity instance()  {
        return ServiceLocator.instance().getInstance(ZanataIdentity.class);
    }

    public void checkLoggedIn() {
        if (!isLoggedIn()) {
            throw new NotLoggedInException();
        }
    }

    public boolean isLoggedIn() {
        return getPrincipal() != null;
    }

    public ZanataCredentials getCredentials() {
        return credentials;
    }

    public Subject getSubject() {
        return subject;
    }

    public void acceptExternallyAuthenticatedPrincipal(Principal principal) {
        if (principal != null) {
            getSubject().getPrincipals().add(principal);
        }
        this.principal = principal;
    }

    /**
     * Accepts an external subject and principal. (Use with caution)
     * This method is used to propagate an authentication context. For
     * example when spawing a new thread for an async task, or when
     * authenticating externally through Kerberos.
     */
    public void acceptExternalSubjectAndPpal(Subject subject,
            Principal principal) {
        this.subject = subject;
        acceptExternallyAuthenticatedPrincipal(principal);
    }

    @PreDestroy
    public void preDestroy() {
        log.debug("preDestroy");
        fireLogoutEvent();
    }

    private void fireLogoutEvent() {
        // NB: avoid using session-scoped beans, because this method is
        // called by preDestroy().
        if (credentials != null && sessionId != null &&
                cachedUsername != null) {
            log.debug(
                    "firing LogoutEvent for user {} with session {} -> {}",
                    cachedUsername,
                    sessionId, this);
            getLogoutEvent().fire(new LogoutEvent(
                    cachedUsername, sessionId, cachedPersonName,
                    cachedPersonEmail));
        }
    }

    public void logout() {
        log.debug("explicit logout");
        fireLogoutEvent();
        if (isLoggedIn()) {
            unAuthenticate();
            HttpSession session =
                    BeanProvider.getContextualReference(HttpSession.class,
                            new AnnotationLiteral<DeltaSpike>() {
                            });
            session.invalidate();
            urlUtil.redirectToInternal(urlUtil.home());
        }
    }

    private Event<LogoutEvent> getLogoutEvent() {
        return logoutEvent;
    }

    public boolean hasRole(String role) {
        if (!securityEnabled)
            return true;
        if (systemOp != null && Boolean.TRUE.equals(systemOp.get()))
            return true;

        tryLogin();

        for (Group sg : getSubject().getPrincipals(Group.class)) {
            if (ROLES_GROUP.equals(sg.getName())) {
                return sg.isMember(new Role(role));
            }
        }
        return false;
    }

    public void checkRole(String role) {
        tryLogin();

        if (!hasRole(role)) {
            if (!isLoggedIn()) {
                // used by org.zanata.security.FacesSecurityEvents.addNotLoggedInMessage()
                getNotLoggedInEvent().fire(new NotLoggedInEvent());
                throw new NotLoggedInException();
            } else {
                throw new AuthorizationException(String.format(
                        "Authorization check failed for role [%s]", role));
            }
        }
    }

    /**
     * Resets all security state and credentials
     */
    public void unAuthenticate() {
        principal = null;
        subject = new Subject();

        credentials.clear();
    }

    // TODO WHY do we have methods hasPermission(target, action) as well as hasPermission(action, target)??? Especially it's used in EL. So DARN confusing!
    public boolean hasPermission(Object target, String action) {
        log.trace("ENTER hasPermission({}, {})", target, action);
        boolean result = resolvePermission(target, action);
        if (result) {
            if (log.isDebugEnabled()) {
                log.debug("ALLOWED hasPermission({}, {}) for user {}",
                        target, action, getAccountUsername());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("DENIED hasPermission({}, {}) for user {}",
                        target, action, getAccountUsername());
            }
        }
        log.trace("EXIT hasPermission(): {}", result);
        return result;
    }

    private boolean resolvePermission(Object target, String action) {
        if (!securityEnabled) {
            return true;
        }
        if (systemOp != null && Boolean.TRUE.equals(systemOp.get())) {
            return true;
        }
        if (permissionResolver == null) {
            return false;
        }
        if (target == null) {
            return false;
        }

        return permissionResolver.hasPermission(target, action);
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
    public boolean hasPermissionWithAnyTargets(String action, Object... targets) {
        return hasPermission(MultiTargetList.fromTargets(targets), action);
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
     * @throws org.zanata.exception.AuthorizationException
     *             if logged in but not authorised
     */
    public void checkPermission(String action, Object... targets) {
        try {
            internalCheckPermission(MultiTargetList.fromTargets(targets),
                    action);
        } catch (AuthorizationException exception) {
            // try to produce a better than default error message
            List<String> meaningfulTargets = Lists.newArrayList();
            for (Object target : targets) {
                if (target instanceof HasUserFriendlyToString) {
                    String targetString = ((HasUserFriendlyToString) target).userFriendlyToString();
                    meaningfulTargets.add(targetString);
                } else {
                    log.warn(
                            "target [{}] may not have user friendly string representation",
                            target.getClass());
                    meaningfulTargets.add(target.toString());
                }
            }
            throw new AuthorizationException(
                    String.format(
                            "Failed to obtain permission('%s') with following facts(%s)",
                            action, meaningfulTargets));
        }
    }

    public void checkPermission(Object target, String action) {
        internalCheckPermission(target, action);
    }

    // based on org.jboss.seam.security.Identity
    private void internalCheckPermission(Object target, String action) {
        if (systemOp != null && Boolean.TRUE.equals(systemOp.get())) return;

        tryLogin();

        if (!hasPermission(target, action)) {
            if (!isLoggedIn()) {
                // used by
                // org.zanata.security.FacesSecurityEvents.addNotLoggedInMessage()
                getNotLoggedInEvent().fire(new NotLoggedInEvent());
                throw new NotLoggedInException();
            } else {
                throw new AuthorizationException(String.format(
                        "Authorization check failed for permission[%s,%s]",
                        target, action));
            }
        }
    }

    private Event<NotLoggedInEvent> getNotLoggedInEvent() {
        return notLoggedInEventEvent;
    }

    // copied from org.jboss.seam.security.Identity.tryLogin()
    public boolean tryLogin() {
        if (!authenticating && getPrincipal() == null && credentials.isSet() &&
                Contexts.isRequestContextActive() &&
                !requestContextValueStore.contains(LOGIN_TRIED)) {
            requestContextValueStore.put(LOGIN_TRIED, true);
            quietLogin();
        }

        return isLoggedIn();
    }

    // copied from org.jboss.seam.security.Identity.quietLogin()
    private void quietLogin() {
        try {
            // N.B. this will trigger Seam's RememberMe functionality and causes
            // a class cast exception (ZanataIdentity is no loger Identity)
//            if (Events.exists()) Events.instance().raiseEvent(Identity.EVENT_QUIET_LOGIN);

            // Ensure that we haven't been authenticated as a result of the EVENT_QUIET_LOGIN event
            if (!isLoggedIn()) {
                if (credentials.isSet()) {
                    authenticate();
                    if (isLoggedIn() && Contexts.isRequestContextActive()) {
                        requestContextValueStore.put(SILENT_LOGIN, true);
                    }
                }
            }
        } catch (LoginException ex) {
            // Quiet login, exceptions are not displayed
        }
    }

    // based on org.jboss.seam.security.Identity.authenticate()
    private synchronized void authenticate() throws LoginException {
        // If we're already authenticated, then don't authenticate again
        if (!isLoggedIn()) {
            principal = null;
            subject = new Subject();
            try {
                authenticating = true;
                preAuthenticate();
                getLoginContext().login();
                postAuthenticate();
            } finally {
                // Set password to null whether authentication is successful or not
                credentials.clearPassword();
                authenticating = false;
            }
        }
    }

    // copied from org.jboss.seam.security.Identity
    private void preAuthenticate() {
        preAuthenticationRoles.clear();
    }

    // copied from org.jboss.seam.security.Identity
    protected void postAuthenticate() {
        // Populate the working memory with the user's principals
        for (Principal p : getSubject().getPrincipals()) {
            if (!(p instanceof Group)) {
                if (principal == null) {
                    principal = p;
                    break;
                }
            }
        }

        if (!preAuthenticationRoles.isEmpty() && isLoggedIn()) {
            for (String role : preAuthenticationRoles) {
                addRole(role);
            }
            preAuthenticationRoles.clear();
        }

        credentials.clearPassword();

        // It's used in:
        // - org.jboss.seam.security.management.JpaIdentityStore.setUserAccountForSession()
        // - org.jboss.seam.security.FacesSecurityEvents.postAuthenticate(Identity)
        // -org.jboss.seam.security.RememberMe.postAuthenticate(Identity)
        // to avoid a class cast exception, we pass Identity here (FacesSecurityEvents is not doing anything with it)
        // We already set authenticatedUser in session so no need to raise this event any more
//        if (Events.exists()) {
//            Events.instance().raiseEvent(Identity.EVENT_POST_AUTHENTICATE,
//                    new Identity());
//        }
    }

    // copied from org.jboss.seam.security.Identity
    public boolean addRole(String role) {
        if (role == null || "".equals(role)) {
            return false;
        }

        if (!isLoggedIn()) {
            preAuthenticationRoles.add(role);
            return false;
        } else {
            for (Group sg : getSubject().getPrincipals(Group.class)) {
                if (ROLES_GROUP.equals(sg.getName())) {
                    return sg.addMember(new Role(role));
                }
            }

            Group roleGroup = new SimpleGroup(ROLES_GROUP);
            roleGroup.addMember(new Role(role));
            getSubject().getPrincipals().add(roleGroup);
            return true;
        }
    }

    public String getJaasConfigName() {
        return jaasConfigName;
    }

    public void setJaasConfigName(String jaasConfigName) {
        this.jaasConfigName = jaasConfigName;
    }

    public LoginContext getLoginContext() throws LoginException {
        if (isApiRequest() || isRequestUsingOAuth()) {
            return new LoginContext(JAAS_DEFAULT, getSubject(),
                    getCredentials().createCallbackHandler(),
                    ZanataConfiguration.INSTANCE);
        }
        if (getJaasConfigName() != null
                && !getJaasConfigName().equals(JAAS_DEFAULT)) {
            return new LoginContext(getJaasConfigName(), getSubject(),
                    getCredentials().createCallbackHandler());
        }

        return new LoginContext(JAAS_DEFAULT, getSubject(), getCredentials()
                .createCallbackHandler(), ZanataConfiguration.INSTANCE);
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
        try {
            if (isLoggedIn()) {
                // If authentication has already occurred during this request
                // via a silent login,
                // and login() is explicitly called then we still want to raise
                // the LOGIN_SUCCESSFUL event,
                // and then return.
                cacheUserDetails();
                if (Contexts.isRequestContextActive()
                        && requestContextValueStore.contains(SILENT_LOGIN)) {
                    getLoginSuccessfulEvent().fire(new LoginSuccessfulEvent(
                            cachedPersonName));
                    this.preAuthenticated = true;
                    return "loggedIn";
                }

                // used by org.zanata.security.FacesSecurityEvents.addAlreadyLoggedInMessage()
                getAlreadyLoggedInEvent().fire(new AlreadyLoggedInEvent());
                this.preAuthenticated = true;
                return "loggedIn";
            }

            authenticate();

            if (!isLoggedIn()) {
                throw new LoginException();
            }

            cacheUserDetails();
            log.debug("Login successful for: {}", cachedUsername);

            // used by org.zanata.security.FacesSecurityEvents.addLoginSuccessfulMessage()
            getLoginSuccessfulEvent().fire(new LoginSuccessfulEvent(
                    cachedPersonName));

            this.preAuthenticated = true;
            return "loggedIn";
        } catch (FailedLoginException | CredentialException | AccountException e) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Login failed for: " + getCredentials().getUsername(),
                        e);
            }
            handleLoginException(e);
        } catch (LoginException e) {
            // if it's not one of the above subclasses, a LoginException may indicate
            // a configuration problem
            log.error(
                    "Login failed for: " + getCredentials().getUsername(),
                    e);
            handleLoginException(e);
        }

        return null;
    }

    /**
     * Caches username and other details, so that we can use them during
     * {@link #preDestroy()}.
     */
    // TODO should we fire an event when username/name/email is changed (rare)?
    private void cacheUserDetails() {
        if (cachedUsername == null) {
            cachedUsername = getCredentials().getUsername();
        }
        if (cachedPersonName == null) {
            HAccount account =
                    accountDAO.getByUsername(cachedUsername);
            if (account != null) {
                HPerson person = account.getPerson();
                if (person != null) {
                    cachedPersonName = person.getName();
                    cachedPersonEmail = person.getEmail();
                } else {
                    cachedPersonEmail = null;
                }
            } else {
                cachedPersonEmail = null;
            }
        }
    }

    private void removeCachedUserDetails() {
        cachedUsername = null;
        cachedPersonEmail = null;
        cachedPersonName = null;
    }

    private void handleLoginException(LoginException e) {
        removeCachedUserDetails();

        // used by org.zanata.security.FacesSecurityEvents.addLoginFailedMessage()
        getLoginFailedEvent().fire(new LoginFailedEvent(e));
    }

    private Event<AlreadyLoggedInEvent> getAlreadyLoggedInEvent() {
        return alreadyLoggedInEventEvent;
    }

    private Event<LoginSuccessfulEvent> getLoginSuccessfulEvent() {
        return loginSuccessfulEventEvent;
    }

    private Event<LoginFailedEvent> getLoginFailedEvent() {
        return loginFailedEventEvent;
    }

    /**
     * Utility method to get the authenticated account username. This differs
     * from org.jboss.seam.security.Credentials.getUsername() in that
     * this returns the actual account's username, not the user provided one
     * (which for some authentication systems is non-existent).
     *
     * @return The currently authenticated account username, or null if the
     *         session is not authenticated.
     */
    @Nullable
    public String getAccountUsername() {
        HAccount authenticatedAccount =
                ServiceLocator.instance().getInstance(HAccount.class,
                        new AuthenticatedLiteral());
        if (authenticatedAccount != null) {
            return authenticatedAccount.getUsername();
        }
        return null;
    }

    public Principal getPrincipal() {
        return principal;
    }

    // copied from org.jboss.seam.security.Identity
    @Override
    public synchronized void runAs(RunAsOperation operation) {
        Principal savedPrincipal = getPrincipal();
        Subject savedSubject = getSubject();

        try {
            principal = operation.getPrincipal();
            subject = operation.getSubject();

            if (systemOp == null) {
                systemOp = new ThreadLocal<>();
            }

            systemOp.set(operation.isSystemOperation());

            operation.execute();
        } finally {
            // Since this bean is a session scoped bean and the threadlocal
            // field is a trancient instance variable, we don't need to worry
            // about removing the value from it (in turns of memory leak in
            // multi-threading environment
            systemOp.set(false);
            principal = savedPrincipal;
            subject = savedSubject;
        }
    }

    @VisibleForTesting
    protected void setPermissionResolver(CustomPermissionResolver resolver) {
        this.permissionResolver = resolver;
    }

    public boolean isRequestUsingOAuth() {
        return requestUsingOAuth;
    }

    public void setRequestUsingOAuth(boolean requestUsingOAuth) {
        this.requestUsingOAuth = requestUsingOAuth;
        // any not null value in password field will do
        getCredentials().setPassword("");
    }

    static class ZanataConfiguration extends
            javax.security.auth.login.Configuration {
        private static final javax.security.auth.login.Configuration INSTANCE =
                new ZanataConfiguration();

        private AppConfigurationEntry[] aces = {
                new AppConfigurationEntry(
                        InternalLoginModule.class.getName(),
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        new HashMap<String, String>()
                )
        };

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            return JAAS_DEFAULT.equals(name) ? aces : null;
        }

        @Override
        public void refresh() {
        }
    }
}
