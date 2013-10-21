/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package org.zanata.servlet;

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

import org.slf4j.MDC;

/**
 * A servlet filter that inserts various values retrieved from the incoming http
 * request into the MDC.
 * <p/>
 * <p/>
 * The values are removed after the request is processed.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
@WebFilter(filterName = "MDCInsertingServletFilter")
public class MDCInsertingServletFilter implements Filter {
    public static final String REQUEST_REMOTE_HOST_MDC_KEY = "req.remoteHost";
    public static final String REQUEST_USER_AGENT_MDC_KEY = "req.userAgent";
    public static final String REQUEST_REQUEST_URI = "req.requestURI";
    public static final String REQUEST_QUERY_STRING = "req.queryString";
    public static final String REQUEST_REQUEST_URL = "req.requestURL";
    public static final String REQUEST_X_FORWARDED_FOR = "req.xForwardedFor";

    public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        insertIntoMDC(request);
        try {
            chain.doFilter(request, response);
        } finally {
            clearMDC();
        }
    }

    private void mdcPut(@Nonnull String key, @Nullable String val) {
        // JBoss logmanager MDC doesn't allow null values
        if (val != null) {
            MDC.put(key, val);
        }
    }

    private void mdcRemove(@Nonnull String key) {
        MDC.remove(key);
    }

    void insertIntoMDC(ServletRequest request) {

        mdcPut(REQUEST_REMOTE_HOST_MDC_KEY, request.getRemoteHost());

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest =
                    (HttpServletRequest) request;
            mdcPut(REQUEST_REQUEST_URI, httpServletRequest.getRequestURI());
            StringBuffer requestURL = httpServletRequest.getRequestURL();
            if (requestURL != null) {
                mdcPut(REQUEST_REQUEST_URL, requestURL.toString());
            }
            mdcPut(REQUEST_QUERY_STRING, httpServletRequest.getQueryString());
            mdcPut(REQUEST_USER_AGENT_MDC_KEY,
                    httpServletRequest.getHeader("User-Agent"));
            mdcPut(REQUEST_X_FORWARDED_FOR,
                    httpServletRequest.getHeader("X-Forwarded-For"));
        }

    }

    void clearMDC() {
        mdcRemove(REQUEST_REMOTE_HOST_MDC_KEY);
        mdcRemove(REQUEST_REQUEST_URI);
        mdcRemove(REQUEST_QUERY_STRING);
        // removing possibly inexistent item is OK
        mdcRemove(REQUEST_REQUEST_URL);
        mdcRemove(REQUEST_USER_AGENT_MDC_KEY);
        mdcRemove(REQUEST_X_FORWARDED_FOR);
    }

    public void init(FilterConfig arg0) throws ServletException {
        // do nothing
    }
}
