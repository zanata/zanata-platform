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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.RestConstant;

import com.google.common.base.Charsets;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

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
public class TraceDebugFilter extends ClientFilter {

    private static final Logger log = LoggerFactory
            .getLogger(TraceDebugFilter.class);

    private boolean logHttp;

    @Override
    public ClientResponse handle(ClientRequest cr)
            throws ClientHandlerException {
        if (!logHttp && !log.isTraceEnabled()) {
            return getNext().handle(cr);
        }
        log(">> REST Request: " + cr.getMethod() + " => "
                + cr.getURI());

        // Log before sending a request
        for (String key : cr.getHeaders().keySet()) {
            String headerVal =
                    cr.getHeaders().get(key).toString();
            if (key.equals(RestConstant.HEADER_API_KEY)) {
                headerVal =
                        this.maskHeaderValues(
                                cr.getHeaders()
                                        .get(key));
            }

            log(">> Header: " + key + " = " + headerVal);
        }
        log(">> body: " + cr.getEntity());



        ClientResponse response = getNext().handle(cr);

        // log after a response has been received
        log("<< REST Response: " + response.getStatus()
                + ":" + response.getClientResponseStatus());
        for (String key : response.getHeaders().keySet()) {
            log("<< Header: " + key + " = " +
                    response.getHeaders().get(key));
        }
        response.bufferEntity();
        log(">> Body: " + getPayloadAsString(response));
        return response;
    }

    // this is jersey implementation specific
    private String getPayloadAsString(ClientResponse response) {
        ByteArrayInputStream entityInputStream = null;
        try {
            entityInputStream =
                    (ByteArrayInputStream) response.getEntityInputStream();
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

    private void log(String msg) {
        if (logHttp) {
            log.info(msg);
        } else {
            log.trace(msg);
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
            maskedList.add(actualValue.toString().replaceAll(".", "*"));
        }

        return maskedList.toString();
    }
}
