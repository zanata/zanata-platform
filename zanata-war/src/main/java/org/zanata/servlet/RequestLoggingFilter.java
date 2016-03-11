/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A servlet filter which logs each request and response.
 *
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@WebFilter(filterName = "RequestLoggingFilter")
public class RequestLoggingFilter implements Filter {
    private static final Logger pageLogger = getLogger(
            "org.zanata.requests.page");
    private static final Logger gwtESLogger = getLogger(
            "org.zanata.requests.gwteventservice");
    private static final Logger resourceLogger = getLogger(
            "org.zanata.requests.resource");

    public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest
                && (pageLogger.isDebugEnabled()
                        || resourceLogger.isDebugEnabled())) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String uri = httpRequest.getRequestURI();
            Logger logger;
            if (uri.equals("/webtrans/gwteventservice")) {
                logger = gwtESLogger;
            } else if (uri.startsWith("/javax.faces.resource/") ||
                    uri.startsWith("/org.richfaces.resources/") ||
                    uri.startsWith("/resources/") ||
                    uri.endsWith(".js") ||
                    uri.endsWith(".css")) {
                logger = resourceLogger;
            } else {
                logger = pageLogger;
            }
            String query = httpRequest.getQueryString();
            String location = (query == null) ? uri : (uri + "?" + query);
            logger.debug("> {} {}", httpRequest.getMethod(), location);
            try {
                chain.doFilter(request, response);
            } finally {
                HttpServletResponse httpResponse =
                        (HttpServletResponse) response;
                String result = String.valueOf(httpResponse.getStatus());
                logger.debug("< {} {} : {}", httpRequest.getMethod(), location, result);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig arg0) throws ServletException {
        // do nothing
    }
}
