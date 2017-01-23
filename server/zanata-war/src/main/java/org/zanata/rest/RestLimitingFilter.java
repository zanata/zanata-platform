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
import javax.inject.Inject;
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
import org.zanata.dao.AccountDAO;
import org.zanata.limits.RateLimitingProcessor;
import org.zanata.model.HAccount;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.zanata.rest.oauth.OAuthUtil;
import org.zanata.util.HttpUtil;
import org.zanata.util.RunnableEx;

/**
 * This class intercepts JAX-RS requests to limit API requests by session, by
 * API key or by OAuth token/code (<strong>without</strong> actually checking
 * their validity, for efficiency). Requests without any credentials will be
 * limited by IP address. Excessive requests up to the hard limit will be
 * queued. Any requests over the hard limit will be rejected.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@WebFilter(filterName = "RestLimitingFilter")
public class RestLimitingFilter implements Filter {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RestLimitingFilter.class);
    private final RateLimitingProcessor processor;
    private final AccountDAO accountDAO;
    private final HAccount authenticatedUser;

    @Inject
    public RestLimitingFilter(RateLimitingProcessor processor,
            AccountDAO accountDAO, HAccount authenticatedUser) {
        this.processor = processor;
        this.accountDAO = accountDAO;
        this.authenticatedUser = authenticatedUser;
    }

    @SuppressWarnings("unused")
    public RestLimitingFilter() {
        this(null, null, null);
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
         * This is only non-null if request came from same browser which user
         * used to logged into Zanata.
         */
        HAccount authenticatedUser = getAuthenticatedUser();
        String apiKey = request.getHeader(HttpUtil.API_KEY_HEADER_NAME);
        // other possible OAuth tokens
        Optional<String> authCodeOpt = OAuthUtil.getAuthCode(request);
        Optional<String> accessTokenOpt =
                OAuthUtil.getAccessTokenFromHeader(request);
        Optional<String> refreshTokenOpt = OAuthUtil.getRefreshToken(request);
        boolean hasAuthenticationCredentials = !Strings.isNullOrEmpty(apiKey)
                || authCodeOpt.isPresent() || accessTokenOpt.isPresent()
                || refreshTokenOpt.isPresent();
        RunnableEx invokeChain = () -> chain.doFilter(req, resp);
        try {
            // authenticatedUser can be from browser or client request
            if (authenticatedUser != null) {
                // this request may come from logged-in browser
                /**
                 * If apiKey is empty, request is either coming from anonymous
                 * user or using OAuth.
                 */
                processor.processForUser(authenticatedUser.getUsername(),
                        response, invokeChain);
            } else if (!hasAuthenticationCredentials) {
                String clientIP = HttpUtil.getClientIp(request);
                processor.processForAnonymousIP(clientIP, response,
                        invokeChain);
            } else {
                // this request may come from REST using api key or OAuth
                String token = authCodeOpt.orElse(
                        accessTokenOpt.orElse(refreshTokenOpt.orElse(apiKey)));
                if (Strings.isNullOrEmpty(apiKey)) {
                    processor.processForToken(token, response, invokeChain);
                } else {
                    processor.processForApiKey(apiKey, response, invokeChain);
                }
            }
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    /**
     * Process anonymous request for rate limiting Note: clientIP might be a
     * proxy server IP address, due to different implementation of each proxy
     * server. This will put all the requests from same proxy server into a
     * single queue.
     */
    @VisibleForTesting
    protected HAccount getAuthenticatedUser() {
        return authenticatedUser;
    }
}
