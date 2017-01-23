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
package org.zanata.test;

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
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * This utility filter provides a second chance for some sort of partially
 * useful logging when the server returns a 500 response (or higher). It logs an
 * ERROR with a stack trace whenever a 5xx response is generated, in case Zanata
 * (or a third party library) somehow returns a 5xx without otherwise logging
 * anything.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@WebFilter(filterName = "BadResponseFilter", urlPatterns = "/*")
public class BadResponseFilter implements Filter {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(BadResponseFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        chain.doFilter(request, new FilteredResponse(response, httpRequest));
    }

    static class FilteredResponse extends HttpServletResponseWrapper {
        private final HttpServletRequest request;

        public FilteredResponse(ServletResponse response,
                HttpServletRequest request) {
            super((HttpServletResponse) response);
            this.request = request;
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            if (sc >= 500) {
                log5xxMessage(sc);
            }
        }

        @Deprecated
        @Override
        public void setStatus(int sc, String sm) {
            super.setStatus(sc, sm);
            if (sc >= 500) {
                log5xxMessage(sc);
            }
        }

        public void log5xxMessage(int statusCode) {
            // We can't access the actual exception which triggered the 500
            // response here, but the problem should be somewhere in the stack
            // trace. There *should* be another log message which includes
            // a stack trace for the real exception, which you can use for
            // problem diagnosis. If not, there is a bug in the error handling
            // too.
            //
            // If debugging a 500 response which has no other logging:
            // 1. Put a breakpoint on the logging call below.
            // 2. Found out which error handling code sent the 500 response
            // (it should be in the stack trace).
            // 3. Fix the error handler so that it always logs an exception
            // as ERROR (with stack trace) for 500 codes.
            // 4. Fix the bug which triggered the exception.
            log.error("Server error response {} for {}", statusCode,
                    getRequestURL(), new Throwable("stack trace"));
        }

        public String getRequestURL() {
            StringBuffer requestURL = request.getRequestURL();
            String queryString = request.getQueryString();
            if (queryString == null) {
                return requestURL.toString();
            } else {
                return requestURL.append('?').append(queryString).toString();
            }
        }
    }
}
