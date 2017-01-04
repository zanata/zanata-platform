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

import com.google.common.base.Throwables;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
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
import org.zanata.security.openid.OpenIdProviderTypeHolder;
import org.zanata.security.openid.YahooOpenIdProvider;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.Contexts;
import org.zanata.util.ServiceLocator;
import org.zanata.util.Synchronized;
import org.zanata.util.UrlUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@SessionScoped
@Synchronized
/*
 * based on org.jboss.seam.security.openid.OpenId class
 */
public class ZanataOpenId implements OpenIdAuthCallback, Serializable {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ZanataOpenId.class);

    @Inject
    private ZanataIdentity identity;

    @Inject
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
    private OpenIdProviderTypeHolder openIdProviderType;

    /**
     * An OpenID string: often a URL, but not always.
     */
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
    protected String authRequest(OpenIdProvider openIdProvider, String userSuppliedString, String returnToUrl) {
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
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl, realm());

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

    // returns a verified id (external username), or null
    @Transactional
    public String verifyResponse(HttpServletRequest httpReq) {
        try {
            // clear any previous messages which might have been generated
            // before the openId authentication took place
            facesMessages.clear();
            /**
             * extract the parameters from the authentication response query string
             * (which comes in as a HTTP request from the OpenID providerType)
             * instead of from httpReq.getParameterMap() to make sure params
             * is encoded correctly bypassing default servlets encoding.
             *
             * httpReq.getParameterMap() failed check in
             * {@link org.openid4java.consumer.ConsumerManager#verifySignature}
             * due to the unicode encoding in URI is different from signature.
             * Reported issue: https://zanata.atlassian.net/browse/ZNTA-1275
             */
            ParameterList respParams =
                ParameterList.createFromQueryString(httpReq.getQueryString());
            AuthSuccess authSuccess = AuthSuccess.createAuthSuccess(respParams);

            // strip existing params (eg dswid)
            String urlWithoutParams = returnToUrl().split("\\?", 2)[0];
            StringBuilder receivingURL = new StringBuilder(urlWithoutParams);
            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                receivingURL.append("?").append(queryString);
            }

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification =
                    manager.verify(receivingURL.toString(), respParams,
                            discovered);

            // The OpenId provider cancelled the authentication
            // TODO shouldn't we check verification.getAuthResponse() instanceof AuthFailure ?
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
                OpenIdProvider openIdProvider = newOpenIdProvider(openIdProviderType.get());
                authResult.setEmail(openIdProvider.getEmail(authSuccess));
                authResult.setFullName(openIdProvider.getFullName(authSuccess));
                authResult.setUsername(openIdProvider.getUsername(authSuccess));
            }

            // invoke the callbacks
            if (callback != null) {
                // CredentialsCreationCallback needs a transaction, but it isn't a CDI bean
                // (so @Transactional won't work on it).
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

    private void authenticate(String username, OpenIdProvider openIdProvider,
            OpenIdAuthCallback callback) {
        String var = openIdProvider.getOpenId(username);
        setId(var);
        setCallback(callback);
        LOGGER.info("openid: {}", getId());
        authenticate(openIdProvider);
    }

    public void login(ZanataCredentials credentials) {
        this.authenticate(credentials, this);
    }

    public void authenticate(ZanataCredentials credentials, OpenIdAuthCallback callback) {
        OpenIdProviderType type = credentials.getOpenIdProviderType();
        openIdProviderType.set(type);
        OpenIdProvider openIdProvider = newOpenIdProvider(type);
        this.authenticate(credentials.getUsername(), openIdProvider, callback);
    }

    private void authenticate(OpenIdProvider openIdProvider) {
        authResult = new OpenIdAuthenticationResult();
        String returnToUrl = returnToUrl();

        String url = authRequest(openIdProvider, id, returnToUrl);

        if (url != null) {
            // TODO [CDI] commented out seam Redirect.captureCurrentView(). verify this still works
//            Redirect redirect = Redirect.instance();
//            redirect.captureCurrentView();
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(url);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
    }

    private String realm() {
        return applicationConfiguration.getServerPath() + "/";
    }

    public String returnToUrl() {
        String url = applicationConfiguration.getServerPath() + "/openid";
        return urlUtil.addWindowId(url);
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

            // TODO check authenticatedAccount != null only once
            if (authenticatedAccount != null
                    && authenticatedAccount.isEnabled()) {
                credentials.setUsername(authenticatedAccount.getUsername());
                ZanataIdentity.instance().acceptExternallyAuthenticatedPrincipal(
                        (new SimplePrincipal(result.getAuthenticatedId())));
                this.loginImmediate();
            } else if (authenticatedAccount != null) {
                credentials.setUsername(authenticatedAccount.getUsername());
            }  else {
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

    private static OpenIdProvider newOpenIdProvider(OpenIdProviderType openIdProviderType) {
        if (openIdProviderType == null) {
            throw new RuntimeException("OpenIdProviderType is null");
        }
        switch (openIdProviderType) {
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
                throw new RuntimeException("Unexpected OpenIdProviderType");
        }
    }
}
