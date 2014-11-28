/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.service.impl;

import javax.annotation.Nonnull;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.zanata.events.JSONType;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Slf4j
public class WebHooksPublisher {

    private static ClientResponse publish(@Nonnull String url,
            @Nonnull String data, @Nonnull MediaType acceptType,
            @Nonnull MediaType mediaType) {
        try {
            ClientRequest request = new ClientRequest(url);
            request.accept(acceptType);
            request.body(mediaType, data);
            return request.post();
        } catch (Exception e) {
            log.error("Error on webHooks post {}", e, url);
            return null;
        }
    }

    public static ClientResponse publish(@Nonnull String url,
        @Nonnull JSONType event) {
        return publish(url, event.getJSON(), MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_JSON_TYPE);
    }
}
