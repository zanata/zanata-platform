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
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.ImmutableList;
import org.jboss.resteasy.annotations.interception.HeaderDecoratorPrecedence;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * This Interceptor is a filter for Zanata REST API requests.
 * It supports <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS">
 * Cross-Origin resource sharing (CORS)</a> by adding {@code Access-Control}
 * headers to REST responses. CORS is needed for requests from different
 * domains, eg when testing alternative front-ends to Zanata.
 * <p>
 * By default, cross origin support is disabled, but the server administrator
 * can enable it by setting the system property zanata.originWhitelist. For
 * instance, in standalone.xml:
 * <pre>
 *   {@code <property name="zanata.originWhitelist" value="http://localhost:8000" />}
 * </pre>
 * </p>
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Provider
@HeaderDecoratorPrecedence
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
            "zanata.originWhitelist";
    private static final String ALLOW_METHODS =
            "PUT, POST, DELETE, GET, OPTIONS";
    private static final ImmutableList<String> originWhitelist;

    static {
        String whitelist = System.getProperty(ZANATA_ORIGIN_WHITELIST, "");
        originWhitelist = ImmutableList.copyOf(whitelist.split(" +"));
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
        throws IOException {
        MultivaluedMap<String, String> requestHeaders =
                requestContext.getHeaders();

        // Client will use these headers for the next request (assuming this is
        // a pre-flight request).
        List<String> nextRequestHeaders = requestHeaders.get("Access-Control-Request-Headers");
        if (nextRequestHeaders == null) {
            nextRequestHeaders = Lists.newArrayList();
        }
        MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();

        // Allow the specified Origin, but only if it is whitelisted.
        String origin = requestContext.getHeaderString("Origin");
        if (!isEmpty(origin) && originWhitelist.contains(origin)) {
            responseHeaders.add("Access-Control-Allow-Origin", origin);
            responseHeaders.add("Vary", "Origin");
        }

        // Allow standard HTTP methods.
        responseHeaders.add("Access-Control-Allow-Methods", ALLOW_METHODS);

        // Allow credentials in requests (eg session cookie).
        // This is potentially very dangerous, so check your Origin!
        responseHeaders.add("Access-Control-Allow-Credentials", true);

        // Allow any requested headers. Again, check your Origin!
        responseHeaders.add("Access-Control-Allow-Headers",
            "X-Requested-With, Content-Type, Accept, " + Joiner.on(",").join(
                nextRequestHeaders));
    }

}
