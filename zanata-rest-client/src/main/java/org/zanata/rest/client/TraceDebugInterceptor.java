/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.RestConstant;

/**
 * Performs logging of Resteasy Requests on the client side. This interceptor
 * logs at the level TRACE, unless the option logHttp is set, in which case it
 * will log as INFO.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 *
 */
@Provider
@ClientInterceptor
public class TraceDebugInterceptor implements ClientExecutionInterceptor {

    private static final Logger log = LoggerFactory
            .getLogger(TraceDebugInterceptor.class);

    private boolean logHttp;

    public TraceDebugInterceptor() {
        this(true);
    }

    public TraceDebugInterceptor(boolean logHttp) {
        this.logHttp = logHttp;
    }

    private void log(String msg) {
        if (logHttp) {
            log.info(msg);
        } else {
            log.trace(msg);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ClientResponse execute(ClientExecutionContext ctx) throws Exception {
        if (!logHttp && !log.isTraceEnabled()) {
            return ctx.proceed();
        }

        log(">> REST Request: " + ctx.getRequest().getHttpMethod() + " => "
                + ctx.getRequest().getUri());

        // Log before sending a request
        for (String key : ctx.getRequest().getHeaders().keySet()) {
            String headerVal =
                    ctx.getRequest().getHeaders().get(key).toString();
            if (key.equals(RestConstant.HEADER_API_KEY)) {
                headerVal =
                        this.maskHeaderValues(ctx.getRequest().getHeaders()
                                .get(key));
            }

            log(">> Header: " + key + " = " + headerVal);
        }
        log(">> Body: " + ctx.getRequest().getBody());

        ClientResponse result = ctx.proceed();

        // log after a response has been received
        log("<< REST Response: " + result.getResponseStatus().getStatusCode()
                + ":" + result.getResponseStatus());
        for (Object key : result.getHeaders().keySet()) {
            log("<< Header: " + key + " = " + result.getHeaders().get(key));
        }

        return result;
    }

    /**
     * Masks a list of header values so they are not displayed as clear text in
     * the logs.
     */
    private String maskHeaderValues(List<String> headerValues) {
        List<String> maskedList = new ArrayList<String>(headerValues.size());

        for (String actualValue : headerValues) {
            maskedList.add(actualValue.replaceAll(".", "*")); // mask all
                                                              // characters with
                                                              // stars
        }

        return maskedList.toString();
    }

}
