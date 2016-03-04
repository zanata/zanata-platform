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

package org.zanata.security.oauth;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.dao.AuthorizationCodeDAO;
import org.zanata.model.HAccount;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.util.FacesNavigationUtil;
import com.google.common.base.Throwables;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Named("authorizeAction")
@CheckLoggedIn
public class AuthorizeAction {
    private static final Logger log =
            LoggerFactory.getLogger(AuthorizeAction.class);

    @Getter
    @Setter
    private String redirectUri;
    @Getter
    @Setter
    private String clientId;

    @Inject
    @DeltaSpike
    private HttpServletRequest request;

    @Inject
    private SecurityTokens securityTokens;

    @Inject
    @Authenticated
    private HAccount authenticatedAccount;

    @Inject
    private AuthorizationCodeDAO authorizationCodeDAO;

    public void authorize() {
        // TODO handle cancel/reject action
        try {
            String username = authenticatedAccount.getUsername();

            String code = securityTokens
                    .generateAuthorizationCode(username, clientId);

            authorizationCodeDAO.persistClientId(username, clientId);

            OAuthResponse resp = OAuthASResponse
                    .authorizationResponse(request,
                            HttpServletResponse.SC_FOUND)
                    .setCode(code)
                    .location(redirectUri)
                    .buildQueryMessage();
            log.info("========== redirect back to:{}", resp.getLocationUri());
            FacesNavigationUtil
                    .redirectToExternal(resp.getLocationUri());
        } catch (OAuthSystemException e) {
            throw Throwables.propagate(e);
        }
    }

    public String getRedirectUriParam() {
        return OAuth.OAUTH_REDIRECT_URI;
    }

    public String getClientIdParam() {
        return OAuth.OAUTH_CLIENT_ID;
    }
}
