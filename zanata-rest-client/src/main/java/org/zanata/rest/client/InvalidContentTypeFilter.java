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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class InvalidContentTypeFilter extends ClientFilter {
    private static final Logger log =
            LoggerFactory.getLogger(InvalidContentTypeFilter.class);
    private static final String ERROR_MSG =
            "Received invalid content type from the server. " +
                    "Most likely you're hitting an error page or being redirected. " +
                    "Please check the server URL is correct (in zanata.ini and in zanata.xml) and make sure you use the correct address.";


    // we assume only xml or json are the valid types (wildcard type is also considered compatible)
    private static final Pattern VALID_TYPES_REGEX =
            Pattern.compile("application/.*\\+?(\\*|xml|json)(;.*)?");

    @Override
    public ClientResponse handle(ClientRequest clientRequest)
            throws ClientHandlerException {
        ClientHandler ch = getNext();
        ClientResponse resp = ch.handle(clientRequest);

        if (resp.getClientResponseStatus().getFamily().equals(
                Response.Status.Family.SUCCESSFUL) &&
                !isContentTypeCompatible(resp.getType())) {
            log.error(ERROR_MSG);
            String title = findPageTitle(resp);
            String snippet = String.format(
                    "Wrong content type received: [%s]. Content page title: [%s]",
                    resp.getType(), title);

            log.error(snippet);
            throw new IllegalStateException(snippet);
        } else {
            return resp;
        }
    }

    @VisibleForTesting
    protected static boolean isContentTypeCompatible(
            final MediaType responseContentType) {
        if (responseContentType == null ||
                responseContentType.isWildcardType() ||
                responseContentType.isWildcardSubtype()) {
            return true;
        }
        // a few end points will return text/plain
        if (MediaType.TEXT_PLAIN_TYPE.isCompatible(responseContentType)) {
            return true;
        }
        Matcher matcher =
                VALID_TYPES_REGEX.matcher(responseContentType.toString());
        return matcher.matches();
    }

    private String findPageTitle(ClientResponse resp) {
        String body = resp.getEntity(String.class).replaceAll("\\n", " ");
        Pattern pattern = Pattern.compile(".*<title>(.*)</title>.*");
        Matcher matcher = pattern.matcher(body);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }
}
