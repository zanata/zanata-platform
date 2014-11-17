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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.zanata.rest.RestConstant;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

@Provider
public class ApiKeyHeaderFilter extends ClientFilter {
    private String apiKey;
    private String username;
    private String ver;

    public ApiKeyHeaderFilter(String username, String apiKey, String ver) {
        this.username = username;
        this.apiKey = apiKey;
        this.ver = ver;
    }

    @Override
    public ClientResponse handle(ClientRequest cr)
            throws ClientHandlerException {
        MultivaluedMap<String, Object> headers = cr.getHeaders();
        headers.add(RestConstant.HEADER_USERNAME, username);
        headers.add(RestConstant.HEADER_API_KEY, apiKey);
        headers.add(RestConstant.HEADER_VERSION_NO, ver);
        return getNext().handle(cr);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
