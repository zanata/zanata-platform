/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import com.google.common.base.Throwables;

/**
 * This class performs an HTTP resource request and offers callback methods for
 * the request's lifecycle.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class ResourceRequest {
    private static final ResourceRequestEnvironment DEFAULT_ENV =
            new ResourceRequestEnvironment();

    private String resourceUrl;
    private String method;
    private ResourceRequestEnvironment environment;

    public ResourceRequest(String resourceUrl, String method) {
        this(resourceUrl, method, DEFAULT_ENV);
    }

    protected ResourceRequest(String resourceUrl, String method,
            ResourceRequestEnvironment environment) {
        this.resourceUrl = resourceUrl;
        this.method = method;
        this.environment = environment;
    }

    protected abstract void prepareRequest(ClientRequest request);

    protected abstract void onResponse(ClientResponse response);

    public void run() throws Exception {
        ClientRequest request = new ClientRequest(resourceUrl);
        request.setHttpMethod(method);
        prepareEnvironment(request);
        prepareRequest(request);
        ClientResponse response = request.execute();
        try {
            onResponse(response);
        } finally {
            response.releaseConnection();
        }
    }

    public ClientResponse runWithResult() {
        ClientRequest request = new ClientRequest(resourceUrl);
        request.setHttpMethod(method);
        prepareEnvironment(request);
        prepareRequest(request);
        try {
            return request.execute();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void prepareEnvironment(ClientRequest request) {
        // Insert the default headers
        if (this.environment.getDefaultHeaders() != null) {
            for (String headerName : this.environment.getDefaultHeaders()
                    .keySet()) {
                request.header(headerName, this.environment.getDefaultHeaders()
                        .get(headerName));
            }
        }
    }
}
