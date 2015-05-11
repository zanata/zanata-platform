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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.lang.StringUtils;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ClientUtil {

    static MultivaluedMap<String, String> asMultivaluedMap(
            String paramKey, Iterable<String> values) {
        MultivaluedMapImpl map = new MultivaluedMapImpl();
        if (values == null) {
            return map;
        }
        for (String extension : values) {
            map.add(paramKey, extension);
        }
        return map;
    }

    public static void checkResult(ClientResponse response) {
        ClientResponse.Status responseStatus =
                response.getClientResponseStatus();
        int statusCode = response.getStatus();

        if (responseStatus == ClientResponse.Status.UNAUTHORIZED) {
            throw new RuntimeException("Incorrect username/password");
        } else if (responseStatus == ClientResponse.Status.SERVICE_UNAVAILABLE) {
            throw new RuntimeException("Service is currently unavailable. " +
                    "Please check outage notification or try again later.");
        } else if (responseStatus == ClientResponse.Status.MOVED_PERMANENTLY
                || statusCode == 302) {
            // if server returns a redirect (most likely due to http to https
            // redirect), we don't want to bury this information in a xml
            // marshalling exception.
            String movedTo = response.getHeaders().getFirst("Location");

            String message;
            if (!Strings.isNullOrEmpty(movedTo)) {
                String baseUrl = getBaseURL(movedTo);
                message = "Server returned a redirect to:" + baseUrl +
                        ". You must change your url option or config file.";
            } else {
                message =
                        "Server returned a redirect. You must change your url option or config file.";
            }
            throw new RuntimeException(message);
        } else if (statusCode >= 399) {
            String annotString = "";
            String uriString = "";
            String entity = "";
            try {
                entity = ": " + response.getEntity(String.class);
            } finally {
                // ignore
            }
            String msg =
                    "operation returned "
                            + statusCode
                            + " ("
                            + Response.Status.fromStatusCode(statusCode) + ")"
                            + entity + uriString
                            + annotString;
            throw new RuntimeException(msg);
        }
    }

    public static String getBaseURL(String movedTo) {
        try {
            URL url = new URI(movedTo).toURL();
            int pathIndex = movedTo.lastIndexOf(url.getPath());
            return movedTo.substring(0, pathIndex) + "/";
        } catch (MalformedURLException | URISyntaxException e) {
            return movedTo;
        }
    }

    /**
     * Extract filename from response header.
     *
     * e.g. Content-Disposition=[attachment; filename="RPM.po"]
     *
     * @param headers
     * @return
     */
    public static String getFileNameFromHeader(MultivaluedMap<String, String> headers) {
        final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
        final String FILENAME_PATTERN = "filename=\"(.*?)\"$";

        String contentDisposition = headers.getFirst(CONTENT_DISPOSITION_HEADER);
        if (StringUtils.isEmpty(contentDisposition)) {
            return null;
        }
        Pattern p = Pattern.compile(FILENAME_PATTERN);
        Matcher m = p.matcher(contentDisposition);
        return m.find() ? m.group(1) : null;
    }
}
