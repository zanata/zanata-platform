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
package org.zanata.rest.client;

import java.net.URI;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RedirectFilter extends ClientFilter {
    private static final Logger log =
            LoggerFactory.getLogger(RedirectFilter.class);

    @Override
    public ClientResponse handle(ClientRequest clientRequest)
            throws ClientHandlerException {
        ClientHandler ch = getNext();
        ClientResponse resp = ch.handle(clientRequest);

        if (resp.getClientResponseStatus().getFamily() !=
                Response.Status.Family.REDIRECTION) {
            return resp;
        } else {
            // try location only if for GET and HEAD
            String method = clientRequest.getMethod();
            if ("HEAD".equals(method) || "GET".equals(method)) {
                log.debug(
                        "Server returns redirection status: {}. Try to follow it",
                        resp.getClientResponseStatus());
                URI redirectTarget = resp.getLocation();
                if (redirectTarget != null) {
                    clientRequest.setURI(redirectTarget);
                }
                return ch.handle(clientRequest);
            } else {
                throw new IllegalStateException(
                        "Received status " + resp.getClientResponseStatus() +
                                ". Check your server URL (e.g. used http instead of https)");
            }
        }
    }
}
