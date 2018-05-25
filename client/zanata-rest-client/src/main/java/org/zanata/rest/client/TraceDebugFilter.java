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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.RestConstant;

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

    private final Logger log;

    private boolean logHttp;

    TraceDebugFilter(boolean logHttp, Logger log) {
        this.logHttp = logHttp;
        this.log = log;
    }

    public TraceDebugFilter(boolean logHttp) {
        this(logHttp, LoggerFactory.getLogger(TraceDebugFilter.class));
    }

    /**
     * Appends an InputStream's data to the StringBuilder (decoding as UTF-8),
     * returning another copy of the data as a second InputStream. (A bit like
     * TeeInputStream.) Warning: uses a lot of memory if the stream is large.
     * <p>
     *     We have to copy to another entity stream, because otherwise the
     *     stream contents we consumed wouldn't be available to the
     *     application.
     * </p>
     */
    private InputStream copyStream(InputStream entityStream, StringBuilder sb)
            throws IOException {
        if (entityStream != null) {
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                IOUtils.copy(entityStream, bytes);
                sb.append(bytes.toString(StandardCharsets.UTF_8));
                return new ByteArrayInputStream(bytes.toByteArray());
            }
        } else {
            sb.append("<null stream>");
            return null;
        }
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
        log(">> Media Type: " + responseContext.getMediaType());
        if (responseContext.hasEntity()) {
            StringBuilder sb = new StringBuilder();
            // replace the response's entity stream with a copy so that we can
            // log the original stream's contents
            responseContext.setEntityStream(
                    copyStream(responseContext.getEntityStream(), sb));
            log(">> Entity:\n" + sb);
        }
    }
}
