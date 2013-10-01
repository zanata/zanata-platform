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

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Credentials;
import org.zanata.security.openid.OpenIdProviderType;

import lombok.extern.slf4j.Slf4j;

/**
 * Overrides the default Seam credentials. Adds app-specific security concepts
 * like authentication mechanisms.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see {@link Credentials}
 */
@Name("org.jboss.seam.security.credentials")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Slf4j
public class ZanataCredentials extends Credentials {
    private AuthenticationType authType;

    private OpenIdProviderType openIdProviderType;

    public AuthenticationType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthenticationType authType) {
        this.authType = authType;
    }

    public OpenIdProviderType getOpenIdProviderType() {
        return openIdProviderType;
    }

    public void setOpenIdProviderType(OpenIdProviderType openIdProviderType) {
        this.openIdProviderType = openIdProviderType;
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public void clear() {
        super.clear();
        authType = null;
        openIdProviderType = null;
    }

    @Override
    public CallbackHandler createCallbackHandler() {
        return new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException,
                    UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        ((NameCallback) callbacks[i]).setName(getUsername());
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        ((PasswordCallback) callbacks[i])
                                .setPassword(getPassword() != null ? getPassword()
                                        .toCharArray() : null);
                    } else if (callbacks[i] instanceof AuthenticationTypeCallback) {
                        ((AuthenticationTypeCallback) callbacks[i])
                                .setAuthType(getAuthType());
                    } else {
                        log.warn("Unsupported callback " + callbacks[i]);
                    }
                }
            }
        };
    }
}
