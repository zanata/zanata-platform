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
import javax.ws.rs.ext.Provider;

import com.google.common.base.Throwables;
import org.apache.commons.lang.StringUtils;
import org.zanata.dao.AccountDAO;
import org.zanata.limits.RateLimitingProcessor;
import org.zanata.model.HAccount;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import org.zanata.security.SecurityFunctions;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.util.HttpUtil;
import org.zanata.util.RunnableEx;
import org.zanata.util.ServiceLocator;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * This class filters JAX-RS calls to limit API calls per
 * API key (via RateLimitingProcessor and RateLimitManager).
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
         * If apiKey is empty, request is from anonymous user,
         * If apiKey is not empty, it must be an authenticated
         * user from pre-process in ZanataRestSecurityInterceptor.
         */
        String apiKey = request.getHeader(HttpUtil.X_AUTH_TOKEN_HEADER);

        // Get user account with apiKey if request is from client
        if(authenticatedUser == null && StringUtils.isNotEmpty(apiKey)) {
            authenticatedUser = getUser(apiKey);
        }

        if (!SecurityFunctions.canAccessRestPath(authenticatedUser,
                request.getMethod(), request.getRequestURI())) {
            /**
             * Not using response.sendError because the app server will generate
             * an HTML page which includes the message. We want to return
             * the message string as is.
             * TODO is this true in a servlet Filter?
             */
//            response.sendError(UNAUTHORIZED.getStatusCode(),
//                    InvalidApiKeyUtil.getMessage(API_KEY_ABSENCE_WARNING));
            response.setStatus(UNAUTHORIZED.getStatusCode());
            response.getOutputStream().write(InvalidApiKeyUtil.getMessage(
                    API_KEY_ABSENCE_WARNING).getBytes());
            return;
        }

        RunnableEx invokeChain = () -> chain.doFilter(req, resp);

        try {
            //authenticatedUser can be from browser or client request
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
                if (!Strings.isNullOrEmpty(authenticatedUser.getApiKey())) {
                    processor.processForApiKey(authenticatedUser.getApiKey(),
                            response, invokeChain);
                } else {
                    processor.processForUser(authenticatedUser.getUsername(),
                            response, invokeChain);
                }
            }
        } catch (IOException | ServletException e) {
            throw e;
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @VisibleForTesting
    protected HAccount getAuthenticatedUser() {
        return ServiceLocator.instance().getInstance(
                HAccount.class, new AuthenticatedLiteral());
    }

    private @Nullable HAccount getUser(@Nonnull String apiKey) {
        return ServiceLocator.instance().getInstance(AccountDAO.class)
                .getByApiKey(apiKey);
    }
}
