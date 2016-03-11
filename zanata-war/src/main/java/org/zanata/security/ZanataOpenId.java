/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.events.LoginCompleted;
import org.zanata.events.PostAuthenticateEvent;
import org.zanata.model.HAccount;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.security.openid.FedoraOpenIdProvider;
import org.zanata.security.openid.GenericOpenIdProvider;
import org.zanata.security.openid.GoogleOpenIdProvider;
import org.zanata.security.openid.MyOpenIdProvider;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdAuthenticationResult;
import org.zanata.security.openid.OpenIdProvider;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.security.openid.YahooOpenIdProvider;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.Contexts;
import org.zanata.util.ServiceLocator;
import org.zanata.util.UrlUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Named("zanataOpenId")
@javax.enterprise.context.SessionScoped

/*
 * based on org.jboss.seam.security.openid.OpenId class
 */
public class ZanataOpenId implements OpenIdAuthCallback, Serializable {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ZanataOpenId.class);

    private ZanataIdentity identity;
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private ZanataCredentials credentials;

    @Inject
    private UserRedirectBean userRedirect;

    @Inject
    private AccountDAO accountDAO;

    @Inject
    private Event<LoginCompleted> loginCompletedEvent;

    @Inject
    private Event<PostAuthenticateEvent> postAuthenticateEvent;

    @Inject
    private UrlUtil urlUtil;

    @Inject
    private OpenIdProvider openIdProvider;

    private String id;
    private OpenIdAuthenticationResult authResult;
    private OpenIdAuthCallback callback;

    private ConsumerManager manager;
    private DiscoveryInformation discovered;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OpenIdAuthenticationResult getAuthResult() {
        return authResult;
    }

    public void setCallback(OpenIdAuthCallback callback) {
        this.callback = callback;
    }

    @SuppressWarnings("rawtypes")
    protected String authRequest(String userSuppliedString, String returnToUrl) {
        try {
            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(userSuppliedString);

            // attempt to associate with the OpenID providerType
            // and retrieve one service endpoint for authentication
            discovered = manager.associate(discoveries);

            // // store the discovery information in the user's session
            // httpReq.getSession().setAttribute("openid-disc", discovered);

            // obtain a AuthRequest message to be sent to the OpenID
            // providerType
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

            Collection<MessageExtension> extensions =
                    openIdProvider.createExtensions();

            // attach the extensions to the authentication request
            for(MessageExtension ext : extensions) {
                authReq.addExtension(ext,
                        openIdProvider.getAliasForExtension(ext));
            }

            return authReq.getDestinationUrl(true);
        } catch (OpenIDException e) {
            LOGGER.warn("exception", e);
        }

        return null;
    }

    public void verify() {
        ExternalContext context =
                javax.faces.context.FacesContext.getCurrentInstance()
                        .getExternalContext();
        HttpServletRequest request = (HttpServletRequest) context.getRequest();

        authResult.setAuthenticatedId(verifyResponse(request));
    }

    public boolean loginImmediately() {
        if (authResult.isAuthenticated()) {
            ZanataIdentity.instance().acceptExternallyAuthenticatedPrincipal(
                    (new SimplePrincipal(authResult.getAuthenticatedId())));
            return true;
        }

        return false;
    }

    public String verifyResponse(HttpServletRequest httpReq) {
        try {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID providerType)
            ParameterList respParams =
                    new ParameterList(httpReq.getParameterMap());
            AuthSuccess authSuccess = AuthSuccess.createAuthSuccess(respParams);

            StringBuilder receivingURL = new StringBuilder(returnToUrl());
            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                receivingURL.append("?").append(httpReq.getQueryString());
            }

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification =
                    manager.verify(receivingURL.toString(), respParams,
                            discovered);

            // The OpenId provider cancelled the authentication
            if ("cancel".equals(respParams.getParameterValue("openid.mode"))) {
                // TODO This should be done at a higher level. i.e. instead of
                // returning a string, return an
                // object that holds more information for the UI to render
                facesMessages.addGlobal("Authentication Request Cancelled");
            }

            // examine the verification result and extract the verified
            // identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                authResult = new OpenIdAuthenticationResult();
                authResult.setAuthenticatedId(verified.getIdentifier());
                authResult.setEmail(openIdProvider.getEmail(authSuccess));
                authResult.setFullName(openIdProvider.getFullName(authSuccess));
                authResult.setUsername(openIdProvider.getUsername(authSuccess));
            }

            // invoke the callbacks
            if (callback != null) {
                callback.afterOpenIdAuth(authResult);
                if (callback.getRedirectToUrl() != null) {
                    userRedirect.setUrl(callback.getRedirectToUrl());
                }
            }

            if (verified != null) {
                return verified.getIdentifier();
            }
        } catch (OpenIDException e) {
            LOGGER.warn("exception", e);
        }

        return null;
    }

    public void logout() {
        init();
    }

    @PostConstruct
    public void init() {
        manager = new ConsumerManager();
        discovered = null;
        id = null;
        authResult = new OpenIdAuthenticationResult();
        // TODO inject these
        identity =
                ServiceLocator.instance().getInstance(ZanataIdentity.class);
        applicationConfiguration =
                ServiceLocator.instance().getInstance(
                        ApplicationConfiguration.class);
    }

    private void loginImmediate() {
        if (loginImmediately()) {
            if (Contexts.isRequestContextActive()) {
                HAccount authenticatedAccount =
                        ServiceLocator.instance().getInstance(
                                HAccount.class, new AuthenticatedLiteral());
                postAuthenticateEvent.fire(new PostAuthenticateEvent(
                        authenticatedAccount));
            }

            // Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL,
            // AuthenticationType.OPENID);
            loginCompletedEvent.fire(new LoginCompleted(AuthenticationType.OPENID));
        }
    }

    private void login(String username, OpenIdProviderType openIdProviderType,
            OpenIdAuthCallback callback) {
        String var = openIdProvider.getOpenId(username);
        setId(var);
        setCallback(callback);
        LOGGER.info("openid: {}", getId());
        login();
    }

    public void login(ZanataCredentials credentials) {
        this.login(credentials, this);
    }

    public void
            login(ZanataCredentials credentials, OpenIdAuthCallback callback) {
        this.login(credentials.getUsername(),
                credentials.getOpenIdProviderType(), callback);
    }

    private void login() {
        authResult = new OpenIdAuthenticationResult();
        String returnToUrl = returnToUrl();

        String url = authRequest(id, returnToUrl);

        if (url != null) {
            // TODO [CDI] commented out seam Redirect.captureCurrentView(). verify this still works
//            Redirect redirect = Redirect.instance();
//            redirect.captureCurrentView();

            urlUtil.redirectTo(url);
        }
    }

    public String returnToUrl() {
        return applicationConfiguration.getServerPath() + "/openid.xhtml";
    }

    /**
     * Default implementation for an authentication callback. This
     * implementations simply authenticates the user locally.
     */
    @Override
    public void afterOpenIdAuth(OpenIdAuthenticationResult result) {
        if (result.isAuthenticated()) {
            HAccount authenticatedAccount =
                    accountDAO.getByCredentialsId(result.getAuthenticatedId());

            identity.setPreAuthenticated(true);

            if (authenticatedAccount != null
                    && authenticatedAccount.isEnabled()) {
                credentials.setUsername(authenticatedAccount.getUsername());
                ZanataIdentity.instance().acceptExternallyAuthenticatedPrincipal(
                        (new SimplePrincipal(result.getAuthenticatedId())));
                this.loginImmediate();
            } else if(authenticatedAccount != null) {
                credentials.setUsername(authenticatedAccount.getUsername());
            }  else if (authenticatedAccount == null) {
                // If the user hasn't been registered yet
                // this is the full open id
                credentials.setUsername(result.getAuthenticatedId());
            }
        }
    }

    /**
     * Default implementation for an authentication callback. This
     * implementation does not provide a redirect url.
     */
    @Override
    public String getRedirectToUrl() {
        return null;
    }

    @Produces
    @RequestScoped
    public OpenIdProvider getOpenIdProvider() {
        switch (credentials.getOpenIdProviderType()) {
            case Fedora:
                return new FedoraOpenIdProvider();

            case Google:
                return new GoogleOpenIdProvider();

            case MyOpenId:
                return new MyOpenIdProvider();

            case Yahoo:
                return new YahooOpenIdProvider();

            case Generic:
                return new GenericOpenIdProvider();

            default:
                return new GenericOpenIdProvider();
        }
    }
}
