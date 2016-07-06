/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.oauth.OAuthUtil;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.oauth.SecurityTokens;
import org.zanata.util.ServiceLocator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author <a href='mailto:pahuang@redhat.com>pahuang</a>
 */
@WebFilter(filterName = "OAuthFilter", urlPatterns = "/authorize/*")
public class OAuthFilter implements Filter {
    private static final Logger log =
            LoggerFactory.getLogger(OAuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse =
                (HttpServletResponse) response;
        Optional<String> oauthRedirect = OAuthUtil.getOAuthRedirectURI(httpServletRequest);
        Optional<String> clientId = OAuthUtil.getOAuthClientId(httpServletRequest);

        if (!clientId.isPresent() || !oauthRedirect.isPresent()) {
            // an OAuth request without client_id or redirect_uri means it's not
            // coming from a third party app. It's possible this request is from
            // the oauth/home.xhtml form submission, or from a bot or invalid
            // third-party request. We will just continue on with the filter
            // chain.
            chain.doFilter(request, response);
            return;
        }

        // we have both client_id and redirect_uri, so the request is from
        // a third-party app trying to do OAuth authorization
        // injection to fields doesn't seem to work
        ZanataIdentity identity = ZanataIdentity.instance();
        SecurityTokens securityTokens = ServiceLocator.instance().getInstance(
                        SecurityTokens.class);


        String authorizationCode = null;
        if (identity.isLoggedIn()) {
            Optional<String> code = securityTokens
                    .findAuthorizationCode(identity.getAccountUsername(),
                            clientId.get());
            if (code.isPresent()) {
                authorizationCode = code.get();
            }
        }

        if (authorizationCode != null) {
            String redirectLocationUrl = securityTokens
                    .getRedirectLocationUrl(httpServletRequest,
                            oauthRedirect.get(), authorizationCode);
            log.debug(
                    "authorization code already issued for this session. Username {}, client id: {}",
                    identity.getAccountUsername(), clientId);
            // redirect back to the requesting app using redirect_uri
            httpServletResponse.sendRedirect(redirectLocationUrl);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
