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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ResponseStatusFilter implements ClientResponseFilter {
    private static final Logger log =
            LoggerFactory.getLogger(ResponseStatusFilter.class);
    @Override
    public void filter(ClientRequestContext requestContext,
            ClientResponseContext responseContext) throws IOException {
        int statusCode = responseContext.getStatus();
        Response.StatusType responseStatus = responseContext.getStatusInfo();

        if (isRedirect(responseStatus)) {
            // if server returns a redirect (most likely due to http to https
            // redirect), we don't want to bury this information in a xml
            // marshalling exception.
            String message =
                    "Server returned a redirect. You must change your url option or config file.";
            // getLocation() can return null but it's acceptable
            throw new RedirectionException(message, statusCode,
                    responseContext.getLocation());
        } else if (statusCode >= 399) {
            URI uri = requestContext.getUri();
            String entity = tryGetEntity(responseContext);
            String msg =
                    generateErrorMessage(statusCode, responseStatus, uri,
                            entity);

            if (responseStatus == Response.Status.UNAUTHORIZED) {
                throw new NotAuthorizedException("Incorrect username/password");
            } else if (responseStatus == Response.Status.NOT_FOUND) {
                // potentially we may continue when 404 happens so we want to release the connection
                responseContext.getEntityStream().close();
                throw new NotFoundException(msg);
            } else if (statusCode < 500) {
                throw new ClientErrorException(msg, statusCode);
            } else if (responseStatus == Response.Status.SERVICE_UNAVAILABLE) {
                throw new ServiceUnavailableException(
                        "Service is currently unavailable. " +
                                "Please check outage notification or try again later.");
            } else {
                throw new ServerErrorException(msg, statusCode);
            }
        }
    }

    private static boolean isRedirect(Response.StatusType statusCode) {
        return statusCode == Response.Status.MOVED_PERMANENTLY
                || statusCode == Response.Status.FOUND;
    }

    private static String tryGetEntity(ClientResponseContext responseContext) {
        String entity = "";
        if (responseContext.hasEntity()) {
            try (BufferedInputStream bufferedIs = new BufferedInputStream(
                    responseContext.getEntityStream());
                    ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = bufferedIs.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                entity = ":" + result.toString("UTF-8");

            } catch (Exception e) {
                log.debug("error reading input stream", e);
            }
        }
        return entity;
    }

    private static String generateErrorMessage(int statusCode,
            Response.StatusType responseStatus, URI uri, String entity) {
        return String.format("operation to [%s] returned %d (%s):%s",
                uri, statusCode, responseStatus, entity);
    }
}
