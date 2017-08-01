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

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resteasy does not support following redirect. https://issues.jboss.org/browse/RESTEASY-1075
 * <p>
 * However there is a workaround. https://github.com/teacurran/sonar-bitbucket/commit/fa978b3d7c0c98ee0bc60c908379fc42dbeae5f4
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RedirectFilter implements ClientResponseFilter {
    private static final Logger log =
            LoggerFactory.getLogger(RedirectFilter.class);

    @Override
    public void filter(ClientRequestContext requestContext,
            ClientResponseContext responseContext) throws IOException {
        // 304 (not modified) is not redirect so we need to be explicit what
        // status codes we want to target here
        if (responseContext.getStatus() == 301 ||
                responseContext.getStatus() == 302) {
            URI redirectTarget = responseContext.getLocation();
            throw new IllegalStateException(
                    String.format(
                            "Received status %s. Redirected to %s. Check your server URL (e.g. used http instead of https)",
                            responseContext.getStatusInfo(), redirectTarget));
        }
    }
}
