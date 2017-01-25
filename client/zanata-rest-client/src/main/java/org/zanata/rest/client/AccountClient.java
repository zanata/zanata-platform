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

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Account;
import javax.annotation.Nullable;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class AccountClient {
    private final RestClientFactory factory;
    private final URI baseUri;

    AccountClient(RestClientFactory factory) {
        this.factory = factory;
        this.baseUri = factory.getBaseUri();
    }

    public @Nullable Account get(String username) {
        try {
            return webResource(username)
                    .get(Account.class);
        } catch (ResponseProcessingException e) {
            if (e.getResponse().getStatusInfo().equals(Response.Status.NOT_FOUND)) {
                return null;
            } else {
                throw e;
            }
        }
    }

    public void put(String username, Account account) {
        Response response = webResource(username)
                .put(Entity.entity(account,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML));
        response.close();
    }

    private Invocation.Builder webResource(String username) {
        return factory.getClient().target(baseUri)
                .path("accounts").path("u")
                .path(username)
                .request(MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
    }
}
