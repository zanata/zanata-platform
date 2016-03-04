/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.service.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.zanata.ApplicationConfiguration;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataCredentials;
import org.zanata.service.AuthenticationService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class AuthenticationServiceImpl implements AuthenticationService {
    @Inject
    private ZanataCredentials credentials;

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Override
    public String authenticate(String username, String password) {
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
}
