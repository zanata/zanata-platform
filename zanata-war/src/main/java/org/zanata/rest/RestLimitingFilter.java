/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest;

import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Throwables;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.zanata.dao.AccountDAO;
import org.zanata.limits.RateLimitingProcessor;
import org.zanata.model.HAccount;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import org.zanata.rest.oauth.OAuthUtil;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.security.oauth.SecurityTokens;
import org.zanata.util.HttpUtil;
import org.zanata.util.RunnableEx;
import org.zanata.util.ServiceLocator;

/**
 * This class filters JAX-RS calls to limit API calls per
 * user if identifiable. Otherwise will limit API calls per IP address
 * (via RateLimitingProcessor and RateLimitManager).
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Slf4j
@WebFilter(filterName = "RestLimitingFilter")
public class RestLimitingFilter implements Filter {
    private static final String API_KEY_ABSENCE_WARNING =
            "You must have a valid API key. You can create one by logging " +
                    "in to Zanata and visiting the settings page.";
    private final RateLimitingProcessor processor;

    public RestLimitingFilter() {
        this(new RateLimitingProcessor());
    }

    @VisibleForTesting
    RestLimitingFilter(RateLimitingProcessor processor) {
        this.processor = processor;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        /**
         * This is only non-null if request came from same browser which
         * user used to logged into Zanata.
         */
        HAccount authenticatedUser = getAuthenticatedUser();

        /**
         * If apiKey is empty, request is either from anonymous user or from
         * OAuth request,
         */
        String apiKey = request.getHeader(HttpUtil.X_AUTH_TOKEN_HEADER);


        // Get user account with apiKey if request is from client
        if(authenticatedUser == null && StringUtils.isNotEmpty(apiKey)) {
            authenticatedUser = getUser(apiKey);
        } else if (authenticatedUser == null) {
            authenticatedUser = tryGetAuthenticatedAccountUsingOAuthTokens(
                    request);
        }

        RunnableEx invokeChain = () -> chain.doFilter(req, resp);

        try {
            // authenticatedUser can be from browser or client request
            if (authenticatedUser == null) {
                /**
                 * Process anonymous request for rate limiting
                 * Note: clientIP might be a proxy server IP address, due to
                 * different implementation of each proxy server. This will put
                 * all the requests from same proxy server into a single queue.
                 */
                String clientIP = HttpUtil.getClientIp(request);
                processor.processForAnonymousIP(clientIP, response, invokeChain);
            } else {
                if (!Strings.isNullOrEmpty(apiKey) && !Strings.isNullOrEmpty(authenticatedUser.getApiKey())) {
                    processor.processForApiKey(authenticatedUser.getApiKey(),
                            response, invokeChain);
                } else {
                    // TODO OAuth request will fall into here. Do we want this?
                    processor.processForUser(authenticatedUser.getUsername(),
                            response, invokeChain);
                }
            }
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private HAccount tryGetAuthenticatedAccountUsingOAuthTokens(
            HttpServletRequest request) {
        String authCode = request.getParameter(OAuth.OAUTH_CODE);
        Optional<String> accessTokenOpt = OAuthUtil.getAccessToken(request);
        Optional<String> usernameOpt = Optional.empty();
        if (StringUtils.isNotEmpty(authCode)) {
            // Get user account with OAuth authorizationCode if it presents
            usernameOpt = getSecurityTokens()
                    .findUsernameForAuthorizationCode(authCode);
        } else if (accessTokenOpt.isPresent()) {
            // Get user account with OAuth accessToken if it presents
            usernameOpt = getSecurityTokens()
                    .matchAccessToken(accessTokenOpt.get());
        }
        if (usernameOpt.isPresent()) {
            return getAccountDAO()
                    .getByUsername(usernameOpt.get());
        }
        return null;
    }

    @VisibleForTesting
    protected SecurityTokens getSecurityTokens() {
        return ServiceLocator.instance().getInstance(
                SecurityTokens.class);
    }

    @VisibleForTesting
    protected HAccount getAuthenticatedUser() {
        return ServiceLocator.instance().getInstance(
                HAccount.class, new AuthenticatedLiteral());
    }

    private @Nullable HAccount getUser(@Nonnull String apiKey) {
        return getAccountDAO()
                .getByApiKey(apiKey);
    }

    @VisibleForTesting
    protected AccountDAO getAccountDAO() {
        return ServiceLocator.instance().getInstance(AccountDAO.class);
    }
}
