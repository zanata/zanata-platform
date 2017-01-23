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
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.exception.AuthorizationException;
import org.zanata.ApplicationConfiguration;
import org.zanata.exception.ZanataServiceException;
import org.zanata.i18n.Messages;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataOpenId;
import org.zanata.service.EmailService;
import org.zanata.service.RegisterService;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.UrlUtil;

/**
 * This action handles new user profile creation.
 */
@Named("newProfileAction")
@ViewScoped
@Model
@Transactional
public class NewProfileAction extends AbstractProfileAction
        implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(NewProfileAction.class);

    private static final long serialVersionUID = 1L;
    @Inject
    private ZanataOpenId zanataOpenId;
    @Inject
    private EmailService emailServiceImpl;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private UrlUtil urlUtil;
    @Inject
    Messages msgs;
    @Inject
    RegisterService registerServiceImpl;
    @Inject
    ApplicationConfiguration applicationConfiguration;

    @PostConstruct
    public void onCreate() {
        AuthenticationType authType = identity.getCredentials().getAuthType();
        if (!identity.isPreAuthenticated()) {
            throw new AuthorizationException(
                    "Need to be in pre authenticated state");
        }
        if (authType != AuthenticationType.OPENID) {
            // Open id user names are url's so they don't make good defaults
            username = identity.getCredentials().getUsername();
        } else {
            // Try to get the information from the openid provider
            username = zanataOpenId.getAuthResult().getUsername();
            name = zanataOpenId.getAuthResult().getFullName();
            email = zanataOpenId.getAuthResult().getEmail();
            // validate username if enforcing matching username is enabled
            if (applicationConfiguration.isEnforceMatchingUsernames()) {
                if (StringUtils.isBlank(username)) {
                    throw new ZanataServiceException(
                            "Server option zanata.enforce.matchingusernames is set, but username from external authentication is missing.");
                } else if (isUsernameTaken(username)) {
                    throw new ZanataServiceException(
                            "Server option zanata.enforce.matchingusernames is set, but username from external authentication is already in use: "
                                    + username);
                } else if (!isUsernameValid(username)) {
                    throw new ZanataServiceException(
                            "Server option zanata.enforce.matchingusernames is set, but username from external authentication is not valid for Zanata: "
                                    + username + ", valid pattern: "
                                    + USERNAME_REGEX);
                }
            }
        }
    }

    /**
     * Make username readonly if - enforce by system property
     * {@link ApplicationConfiguration#isEnforceMatchingUsernames()}
     */
    public boolean isReadOnlyUsername() {
        return applicationConfiguration.isEnforceMatchingUsernames();
    }

    /**
     * Manual check if username is valid pattern
     * {@link HasUserDetail#USERNAME_REGEX}
     */
    private boolean isUsernameValid(String username) {
        Pattern p = Pattern.compile(USERNAME_REGEX);
        return p.matcher(username).matches();
    }

    @Transactional
    public String createUser() {
        this.valid = true;
        validateEmail(this.email);
        validateUsername(username);
        if (!this.isValid()) {
            return "failure";
        }
        String key;
        AuthenticationType authType = identity.getCredentials().getAuthType();
        if (authType == AuthenticationType.KERBEROS
                || authType == AuthenticationType.JAAS) {
            key = registerServiceImpl.register(this.username, this.username,
                    this.email);
        } else {
            key = registerServiceImpl.register(this.username,
                    zanataOpenId.getAuthResult().getAuthenticatedId(),
                    AuthenticationType.OPENID, this.name, this.email);
        }
        String message = emailServiceImpl.sendActivationEmail(this.name,
                this.email, key);
        identity.unAuthenticate();
        facesMessages.addGlobal(message);
        return "success";
    }

    public void cancel() {
        identity.logout();
        urlUtil.redirectToInternal(urlUtil.home());
    }
}
