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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.ImmutableList;

import com.google.common.base.Joiner;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * This Interceptor is a filter for Zanata REST API requests.
 * It supports <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS">
 * Cross-Origin resource sharing (CORS)</a> by adding {@code Access-Control}
 * headers to REST responses. CORS is needed for requests from different
 * domains, eg when testing alternative front-ends to Zanata.
 * <p>
 * By default, cross origin support is disabled, but the server administrator
 * can enable it by setting the system property zanata.origin.whitelist. For
 * instance, in standalone.xml:
 * <pre>
 *   {@code <property name="zanata.origin.whitelist" value="http://localhost:8000" />}
 * </pre>
 * </p>
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
//
//
//
// WARNING: Misuse of CORS headers can expose Zanata to CSRF attacks.
// Please read up on CORS and CSRF before changing this file!
//
//
//
public class ZanataRestResponseInterceptor implements ContainerResponseFilter {
    private static final String ZANATA_ORIGIN_WHITELIST =
            "zanata.origin.whitelist";
    private static final String ALLOW_METHODS =
            "PUT, POST, DELETE, GET, OPTIONS";
    private static final ImmutableList<String> originWhitelist;

    static {
        String whitelist = System.getProperty(ZANATA_ORIGIN_WHITELIST, "");
        originWhitelist = ImmutableList.copyOf(whitelist.split(" +"));
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
        throws IOException {
        if (originWhitelist.isEmpty()) {
            return;
        }

        MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();
        // Response will be different if Origin request header is different:
        responseHeaders.add("Vary", "Origin");

        // Allow the specified Origin, but only if it is whitelisted.
        String origin = requestContext.getHeaderString("Origin");
        if (!isEmpty(origin) && originWhitelist.contains(origin)) {
            responseHeaders.add("Access-Control-Allow-Origin", origin);

            // Allow standard HTTP methods.
            responseHeaders.add("Access-Control-Allow-Methods", ALLOW_METHODS);

            // Allow credentials in requests (eg session cookie).
            // This is potentially very dangerous, so check your Origin!
            responseHeaders.add("Access-Control-Allow-Credentials", true);

            // Client will use these headers for the next request (assuming this is
            // a pre-flight request).
            List<String> nextRequestHeaders = requestContext.getHeaders().getOrDefault(
                    "Access-Control-Request-Headers", emptyList());
            Set<String> allowedHeaders = new HashSet<>(nextRequestHeaders);
            allowedHeaders.add("X-Requested-With");
            allowedHeaders.add("Content-Type");
            allowedHeaders.add("Accept");

            // Allow any requested headers. Again, check your Origin!
            responseHeaders.add("Access-Control-Allow-Headers",
                    Joiner.on(",").join(allowedHeaders));
        }
    }

}
