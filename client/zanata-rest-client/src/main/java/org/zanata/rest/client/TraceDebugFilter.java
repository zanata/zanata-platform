/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.RestConstant;

import com.google.common.base.Charsets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Performs logging of Requests on the client side. This interceptor
 * logs at the level TRACE, unless the option logHttp is set, in which case it
 * will log as INFO.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 *
 */
@Provider
public class TraceDebugFilter implements ClientRequestFilter,
        ClientResponseFilter {

    private static final Logger log = LoggerFactory
            .getLogger(TraceDebugFilter.class);

    private boolean logHttp;

    private String getPayloadAsString(ClientResponseContext response) {
        ByteArrayInputStream entityInputStream = null;
        try {
            entityInputStream =
                    (ByteArrayInputStream) response.getEntityStream();
            int available = entityInputStream.available();
            byte[] data = new byte[available];
            entityInputStream.read(data);
            return new String(data, 0, available, Charsets.UTF_8);
        } catch (Exception e) {
            log.warn("can't read response payload");
            return "[error reading response]";
        } finally {
            if (entityInputStream != null) {
                entityInputStream.reset();
            }
        }

    }

    public TraceDebugFilter(boolean logHttp) {
        this.logHttp = logHttp;
    }

    @SuppressFBWarnings(value = "SLF4J_FORMAT_SHOULD_BE_CONST")
    void log(String formattedMessage) {
        if (logHttp) {
            log.info(formattedMessage);
        } else {
            log.trace(formattedMessage);
        }
    }

    /**
     * Masks a list of header values so they are not displayed as clear text in
     * the logs.
     */
    private String maskHeaderValues(List<Object> headerValues) {
        List<String> maskedList = new ArrayList<String>(headerValues.size());

        for (Object actualValue : headerValues) {
            // mask all characters with stars
            //noinspection ReplaceAllDot
            maskedList.add(actualValue.toString().replaceAll(".", "*"));
        }

        return maskedList.toString();
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (!logHttp && !log.isTraceEnabled()) {
            return;
        }
        log(">> REST Request: " + requestContext.getMethod() + " => "
                + requestContext.getUri());

        // Log before sending a request
        for (String key : requestContext.getHeaders().keySet()) {
            String headerVal =
                    requestContext.getHeaders().get(key).toString();
            if (key.equals(RestConstant.HEADER_API_KEY)) {
                headerVal =
                        this.maskHeaderValues(
                                requestContext.getHeaders()
                                        .get(key));
            }

            log(">> Header: " + key + " = " + headerVal);
        }
        log(">> body: " + requestContext.getEntity());
    }

    @Override
    public void filter(ClientRequestContext requestContext,
            ClientResponseContext responseContext) throws IOException {
        if (!logHttp && !log.isTraceEnabled()) {
            return;
        }
        // log after a response has been received
        log("<< REST Response: " + responseContext.getStatus()
                + ":" + responseContext.getStatusInfo());
        for (String key : responseContext.getHeaders().keySet()) {
            log("<< Header: " + key + " = " +
                    responseContext.getHeaders().get(key));
        }
        log(">> Body: " + getPayloadAsString(responseContext));
    }
}
