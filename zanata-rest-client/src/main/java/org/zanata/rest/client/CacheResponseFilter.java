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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * This was a workaround created because jersey client was erroneously
 * believed not to support put/post returning
 * response.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @deprecated use builder.put/post(ClientResponse.class, entity)
 */
@Deprecated
class CacheResponseFilter extends ClientFilter {
    private Optional<ClientResponse> cachedClientResponse = Optional.absent();

    @Override
    public ClientResponse handle(ClientRequest cr)
            throws ClientHandlerException {
        ClientResponse response = getNext().handle(cr);
        response.bufferEntity();
        cachedClientResponse = Optional.of(response);
        return response;
    }

    public <T> T getEntity(Class<T> type) {
        return getResponse().getEntity(type);
    }

    public <T> T getEntity(GenericType<T> genericType) {
        return getResponse().getEntity(genericType);
    }

    public ClientResponse getResponse() {
        checkState();
        return cachedClientResponse.get();
    }

    private void checkState() {
        Preconditions.checkState(cachedClientResponse.isPresent(),
                "No cached ClientResponse. Did you forget to add this filter?");
    }
}
