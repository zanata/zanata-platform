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

import java.util.List;
import org.apache.commons.lang.StringUtils;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.CredentialsDAO;
import org.zanata.events.LoginCompleted;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.security.HCredentials;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.service.UserAccountService;
import org.zanata.ui.faces.FacesMessages;
import javax.enterprise.event.Observes;

/**
 * Centralizes all attempts to authenticate locally or externally.
 *
 * The authenticate methods will perform the authentication but will not login
 * the authenticated user against the session. The login methods will perform
 * these two steps.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("authenticationManager")
@RequestScoped
public class AuthenticationManager {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AuthenticationManager.class);

    @Inject
    private ZanataIdentity identity;
    @Inject
    private ZanataJpaIdentityStore identityStore;
    @Inject
    private ZanataCredentials credentials;
    @Inject
    private ZanataOpenId zanataOpenId;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private UserAccountService userAccountServiceImpl;
    @Inject
    private CredentialsDAO credentialsDAO;
    @Inject
    private AccountDAO accountDAO;
    @Inject
    private UserRedirectBean userRedirect;
    @Inject
    private ApplicationConfiguration applicationConfiguration;
    @Inject
    private Messages msgs;
    @Inject
    private SpNegoIdentity spNegoIdentity;

    /**
     * Logs in a user using a specified authentication type.
     *
     * @param authenticationType
     *            Authentication type to use.
     * @param username
     *            User's name.
     * @param password
     *            User's password. May be null for some authentication types.
     * @return A String with the result of the operation.
     */
    private String login(AuthenticationType authenticationType, String username,
            String password) {
        credentials.setUsername(username);
        credentials.setPassword(password);
        String result = identity.login(authenticationType);
        if (isLoggedIn(result)) {
            this.onLoginCompleted(new LoginCompleted(authenticationType));
        }
        return result;
    }

    private boolean isLoggedIn(String result) {
        return result != null && result.equals("loggedIn");
    }

    /**
     * Logs in user with internal authentication type
     *
     * @return
     */
    public String internalLogin() {
        if (isAuthenticatedAccountWaitingForActivation()) {
            return "inactive";
        }
        return login(AuthenticationType.INTERNAL, credentials.getUsername(),
                credentials.getPassword());
    }

    /**
     * Logs in user with jaas authentication type
     *
     * @return
     */
    public String jaasLogin() {
        String result = login(AuthenticationType.JAAS,
                credentials.getUsername(), credentials.getPassword());
        if (isLoggedIn(result)) {
            if (isAuthenticatedAccountWaitingForActivation()) {
                return "inactive";
            }
        }
        return result;
    }

    /**
     * Logs in with the kerberos authentication type using ticket based
     * authentication.
     */
    public void kerberosLogin() {
        if (applicationConfiguration.isKerberosAuth()) {
            spNegoIdentity.authenticate();
            if (!isNewUser() && !isAuthenticatedAccountWaitingForActivation()
                    && isAccountEnabledAndActivated()) {
                spNegoIdentity.login();
                this.onLoginCompleted(
                        new LoginCompleted(AuthenticationType.KERBEROS));
            }
        }
    }

    /**
     * Logs in with kerberos using from based (username/password) authentication
     */
    public String formBasedKerberosLogin() {
        if (applicationConfiguration.isKerberosAuth()) {
            String loginResult = this.login(AuthenticationType.KERBEROS,
                    credentials.getUsername(), credentials.getPassword());
            if (isAuthenticatedAccountWaitingForActivation()) {
                loginResult = "inactive";
            }
            return loginResult;
        }
        return null;
    }

    /**
     * Logs in an Open Id user. Uses the values set in {@link ZanataCredentials}
     * for authentication. This method should be invoked to authenticate AND log
     * a user into Zanata.
     *
     * @return A String with the result of the operation.
     */
    public String openIdLogin() {
        String loginResult = identity.login(AuthenticationType.OPENID);
        return loginResult;
    }

    /**
     * Authenticates an Open Id user. This method <b>will not</b> log in the
     * authenticated user. Because control needs to be handled over to the Open
     * Id provider, a callback may be provided to perform actions after the
     * authentication attempt is finished.
     *
     * @param openIdProviderType
     *            Open Id provider to use for authentication
     * @param callback
     *            Contains the logic to execute after the authentication
     *            attempt.
     */
    public void openIdAuthenticate(OpenIdProviderType openIdProviderType,
            OpenIdAuthCallback callback) {
        openIdAuthenticate(null, openIdProviderType, callback);
    }

    /**
     * Authenticates an Open Id user. This method <b>will not</b> log in the
     * authenticated user. Because control needs to be handled over to the Open
     * Id provider, a callback may be provided to perform actions after the
     * authentication attempt is finished.
     *
     * @param openId
     *            The Open Id identifier to be used.
     * @param openIdProviderType
     *            Open Id provider to use for authentication
     * @param callback
     *            Contains the logic to execute after the authentication
     *            attempt.
     */
    public void openIdAuthenticate(String openId,
            OpenIdProviderType openIdProviderType,
            OpenIdAuthCallback callback) {
        ZanataCredentials volatileCreds = new ZanataCredentials();
        volatileCreds.setUsername(openId);
        volatileCreds.setAuthType(AuthenticationType.OPENID);
        volatileCreds.setOpenIdProviderType(openIdProviderType);
        zanataOpenId.authenticate(volatileCreds, callback);
    }

    /**
     * This method indicates where a user needs to be redirected for security
     * purposes. It should be used to determine where to direct a user when they
     * try to access secured content.
     *
     * @return A string containing a hint of where to redirect the user. <br/>
     *         Valid values are: <br/>
     *         edit - Redirect the user to the edit profile page.<br/>
     *         redirect - Allow the user to continue to the page they originally
     *         aimed for.<br/>
     *         home - Redirect the user to the home page.<br/>
     *         inactive - The user's account is inactive.<br/>
     *         login - Redirect the user to the login page.<br/>
     *         dashboard - Redirect the user to dashboard page.
     */
    public String getAuthenticationRedirect() {
        if (identity.getCredentials()
                .getAuthType() == AuthenticationType.KERBEROS) {
            if (isAuthenticatedAccountWaitingForActivation()) {
                return "inactive";
            } else if (identity.isPreAuthenticated() && isNewUser()) {
                return "edit";
            } else if (identity.isLoggedIn()) {
                if (userRedirect != null) {
                    if (userRedirect.isRedirect()
                            && !userRedirect.isRedirectToHome()
                            && !userRedirect.isRedirectToError()
                            && !userRedirect.isRedirectToRegister()) {
                        return "redirect";
                    }
                }
                return "dashboard";
            }
            return "home";
        } else {
            return "login";
        }
    }

    /**
     * Performs operations after a successful login is completed. Currently runs
     * the role assignment rules on the logged in account.
     *
     * @param payload
     *            contains Authentication type that was used to login.
     */
    public void onLoginCompleted(@Observes LoginCompleted payload) {
        AuthenticationType authType = payload.getAuthType();
        identity.setPreAuthenticated(true);
        if (isExternalLogin() && !isNewUser()
                && isAccountEnabledAndActivated()) {
            applyAuthentication();
        }
        // Get the authenticated account and credentials
        HAccount authenticatedAccount = null;
        HCredentials authenticatedCredentials = null;
        String username = credentials.getUsername();
        if (authType == AuthenticationType.OPENID) {
            authenticatedCredentials = credentialsDAO.findByUser(
                    zanataOpenId.getAuthResult().getAuthenticatedId());
            // on first Open Id login, there might not be any stored credentials
            if (authenticatedCredentials != null) {
                authenticatedAccount = authenticatedCredentials.getAccount();
            }
        } else {
            authenticatedCredentials = credentialsDAO.findByUser(username);
            authenticatedAccount = accountDAO.getByUsername(username);
        }
        if (authenticatedAccount != null) {
            userAccountServiceImpl.runRoleAssignmentRules(authenticatedAccount,
                    authenticatedCredentials, authType.name());
        }
    }

    public boolean isAccountWaitingForActivation(String username) {
        HAccount account = accountDAO.getByUsername(username);
        return account != null && account.getAccountActivationKey() != null;
    }

    public boolean isAccountEnabled(String username) {
        return !StringUtils.isEmpty(username)
                && identityStore.isUserEnabled(username);
    }

    public boolean isAuthenticatedAccountWaitingForActivation() {
        boolean userIsAuthenticated = true;
        // For internal Authentication, the user must be re-authenticated
        // without
        // taking into account
        // the account's enabled flag
        if (credentials.getAuthType() == AuthenticationType.INTERNAL
                && applicationConfiguration.isInternalAuth()) {
            userIsAuthenticated = identityStore.checkPasswordIgnoringActivation(
                    credentials.getUsername(), credentials.getPassword());
        }
        return userIsAuthenticated
                && !isAccountEnabled(credentials.getUsername())
                && isAccountWaitingForActivation(credentials.getUsername());
    }

    public boolean isNewUser(String username) {
        return identityStore.isNewUser(username);
    }

    public boolean isNewUser() {
        if (credentials.getAuthType() == AuthenticationType.OPENID
                && applicationConfiguration.isOpenIdAuth()) {
            return credentialsDAO.findByUser(
                    zanataOpenId.getAuthResult().getAuthenticatedId()) == null;
        }
        return isNewUser(credentials.getUsername());
    }

    public void setAuthenticateUser(String username) {
        HAccount user = identityStore.lookupUser(username);
        identityStore.setAuthenticateUser(user);
    }

    public List<String> getImpliedRoles(String username) {
        return identityStore.getImpliedRoles(username);
    }

    public boolean isAuthenticated() {
        if (credentials.getAuthType() == AuthenticationType.OPENID
                && applicationConfiguration.isOpenIdAuth()) {
            return zanataOpenId.getAuthResult().isAuthenticated();
        }
        return identity.isLoggedIn();
    }

    private boolean isAccountEnabledAndActivated() {
        String username = identity.getCredentials().getUsername();
        if (isAccountEnabled(username)) {
            return true;
        } else {
            String message = "";
            if (isAccountWaitingForActivation(username)) {
                message = msgs.get("authentication.loginFailed");
            } else {
                message = "User " + username
                        + " has been disabled. Please contact server admin.";
            }
            facesMessages.clear();
            facesMessages.addGlobal(message);
            // identity.setPreAuthenticated(false);
            // identity.unAuthenticate();
            return false;
        }
    }

    private boolean isExternalLogin() {
        return identity.getCredentials()
                .getAuthType() != AuthenticationType.INTERNAL
                && !identity.isApiRequest();
    }

    private void applyAuthentication() {
        String username = identity.getCredentials().getUsername();
        for (String role : getImpliedRoles(username)) {
            identity.addRole(role);
        }
        setAuthenticateUser(username);
    }
}
