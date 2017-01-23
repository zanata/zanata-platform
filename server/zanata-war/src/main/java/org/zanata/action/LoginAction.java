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

import java.io.IOException;
import java.io.Serializable;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.ApplicationConfiguration;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.AuthenticationType;
import org.zanata.security.UserRedirectBean;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.openid.FedoraOpenIdProvider;
import org.zanata.security.openid.GoogleOpenIdProvider;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.security.openid.YahooOpenIdProvider;
import org.zanata.util.FacesNavigationUtil;

/**
 * This action takes care of logging a user into the system. It contains logic
 * to handle the different authentication mechanisms offered by the system.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("loginAction")
@ViewScoped
@Model
@Transactional
public class LoginAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LoginAction.class);

    private static final long serialVersionUID = 1L;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private ZanataCredentials credentials;
    @Inject
    private AuthenticationManager authenticationManager;
    @Inject
    private ApplicationConfiguration applicationConfiguration;
    private String username;
    private String password;
    private String openId = "http://";
    @Inject
    private UserRedirectBean userRedirect;
    @Inject
    private AuthenticationType authenticationType;

    public String login() {
        credentials.setUsername(username);
        credentials.setPassword(password);
        credentials.setAuthType(authenticationType);
        String loginResult;
        switch (credentials.getAuthType()) {
        case INTERNAL:
            loginResult = authenticationManager.internalLogin();
            break;

        case JAAS:
            loginResult = authenticationManager.jaasLogin();
            break;

        case KERBEROS:
            // Ticket based kerberos auth happens when hittin klogin
            // (see pages.xml)
            loginResult = authenticationManager.formBasedKerberosLogin();
            break;

        default:
            throw new RuntimeException(
                    "login() only supports internal, jaas, or kerberos authentication");

        }
        if ("loggedIn".equals(loginResult)) {
            if (authenticationManager.isAuthenticated()
                    && authenticationManager.isNewUser()) {
                return "createUser";
            }
            if (authenticationManager.isAuthenticated()
                    && !authenticationManager.isNewUser()
                    && userRedirect.shouldRedirectToDashboard()) {
                return "dashboard";
            }
            if (authenticationManager.isAuthenticated()
                    && !authenticationManager.isNewUser()
                    && userRedirect.isRedirect()) {
                // TODO [CDI] seam will create a conversation when you return
                // view id directly or redirect to external url
                return continueToPreviousUrl();
            }
        } else if ("inactive".equals(loginResult)) {
            // TODO [CDI] commented out programmatically ending conversation
            // Conversation.instance().end();
            return "inactive";
        }
        return loginResult;
    }

    private String continueToPreviousUrl() {
        try {
            FacesNavigationUtil.redirect(FacesContext.getCurrentInstance(),
                    userRedirect.getUrl());
        } catch (IOException e) {
            return "dashboard";
        }
        return "continue";
    }

    /**
     * Only for open id.
     *
     * @param authProvider
     *            Open Id authentication provider.
     */
    public String openIdLogin(String authProvider) {
        OpenIdProviderType providerType =
                OpenIdProviderType.valueOf(authProvider);
        if (providerType == OpenIdProviderType.Generic) {
            credentials.setUsername(openId);
        }
        credentials.setAuthType(AuthenticationType.OPENID);
        credentials.setOpenIdProviderType(providerType);
        return authenticationManager.openIdLogin();
    }

    /**
     * Another way of doing open id without knowing the provider first hand.
     * Tries to match the given open id with a known provider. If it can't find
     * one it uses a generic provider.
     */
    public String genericOpenIdLogin(String openId) {
        setOpenId(openId);
        OpenIdProviderType providerType = getBestSuitedProvider(openId);
        return openIdLogin(providerType.name());
    }

    /**
     * Returns the best suited provider for a given Open id.
     *
     * @param openId
     *            An Open id (They are usually in the form of a url).
     */
    public static OpenIdProviderType getBestSuitedProvider(String openId) {
        if (new FedoraOpenIdProvider().accepts(openId)) {
            return OpenIdProviderType.Fedora;
        } else if (new GoogleOpenIdProvider().accepts(openId)) {
            return OpenIdProviderType.Google;
        } else if (new YahooOpenIdProvider().accepts(openId)) {
            return OpenIdProviderType.Yahoo;
        } else {
            return OpenIdProviderType.Generic;
        }
    }

    /**
     * Indicates which location a user should be redirected when accessing the
     * login page.
     *
     * @return A string indicating where the user should be redirected when
     *         trying to access the login page.
     */
    public String getLoginPageRedirect() {
        if (identity.isLoggedIn()) {
            return "dashboard";
        }
        if (applicationConfiguration.isOpenIdAuth()
                && applicationConfiguration.isSingleOpenIdProvider()) {
            // go directly to the provider's login page
            return genericOpenIdLogin(
                    applicationConfiguration.getOpenIdProviderUrl());
        }
        return "login";
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getOpenId() {
        return this.openId;
    }

    public void setOpenId(final String openId) {
        this.openId = openId;
    }
}
