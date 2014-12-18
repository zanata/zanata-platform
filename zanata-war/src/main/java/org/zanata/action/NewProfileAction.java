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
package org.zanata.action;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.zanata.ApplicationConfiguration;
import org.zanata.i18n.Messages;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataOpenId;
import org.zanata.service.EmailService;
import org.zanata.service.RegisterService;
import org.zanata.ui.faces.FacesMessages;

/**
 * This action handles new user profile creation.
 *
 */
@Name("newProfileAction")
@Scope(ScopeType.PAGE)
@Slf4j
public class NewProfileAction extends AbstractProfileAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private ZanataOpenId zanataOpenId;

    @In
    private EmailService emailServiceImpl;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    Messages msgs;

    @In
    RegisterService registerServiceImpl;

    @In
    ApplicationConfiguration applicationConfiguration;

    @Create
    public void onCreate() {
        if (identity.getCredentials().getAuthType() != AuthenticationType.OPENID) {
            // Open id user names are url's so they don't make good defaults
            username = identity.getCredentials().getUsername();
        }
        String domain = applicationConfiguration.getDomainName();
        if (domain == null) {
            email = "";
        } else {
            if (applicationConfiguration.isOpenIdAuth()) {
                email = zanataOpenId.getAuthResult().getEmail();
            } else {
                email = identity.getCredentials().getUsername() + "@" + domain;
            }
        }
    }

    @Transactional
    public void createUser() {
        this.valid = true;
        validateEmail(this.email);
        validateUsername(username);

        if (!this.isValid()) {
            return;
        }

        String key;
        AuthenticationType authType = identity.getCredentials().getAuthType();
        if (authType == AuthenticationType.KERBEROS
                || authType == AuthenticationType.JAAS) {
            key = registerServiceImpl.register(
                    this.username, this.username, this.email);
        } else {
            key = registerServiceImpl.register(this.username, zanataOpenId
                            .getAuthResult().getAuthenticatedId(),
                            AuthenticationType.OPENID, this.name, this.email);
        }
        String message =
                emailServiceImpl.sendActivationEmail(this.name, this.email, key);
        identity.unAuthenticate();
        facesMessages.addGlobal(message);
    }

    public void cancel() {
        identity.logout();
    }

}
