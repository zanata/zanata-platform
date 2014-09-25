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

import lombok.Getter;
import lombok.Setter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.zanata.ApplicationConfiguration;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.openid.FedoraOpenIdProvider;
import org.zanata.security.openid.GoogleOpenIdProvider;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.security.openid.YahooOpenIdProvider;

/**
 * This action takes care of logging a user into the system. It contains logic
 * to handle the different authentication mechanisms offered by the system.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("loginAction")
@Scope(ScopeType.PAGE)
public class LoginAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private Identity identity;

    @In
    private ZanataCredentials credentials;

    @In
    private AuthenticationManager authenticationManager;

    @In
    private ApplicationConfiguration applicationConfiguration;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String openId = "http://";

    public String login() {
        credentials.setUsername(username);
        credentials.setPassword(password);
        if (applicationConfiguration.isInternalAuth()) {
            credentials.setAuthType(AuthenticationType.INTERNAL);
        } else if (applicationConfiguration.isJaasAuth()) {
            credentials.setAuthType(AuthenticationType.JAAS);
        } else if (applicationConfiguration.isKerberosAuth()) {
            credentials.setAuthType(AuthenticationType.KERBEROS);
        }

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

        return loginResult;
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
            return genericOpenIdLogin(applicationConfiguration
                    .getOpenIdProviderUrl());
        }
        return "login";
    }
}
