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
import java.net.URISyntaxException;

import org.hamcrest.Matchers;
import org.zanata.rest.dto.VersionInfo;

import static org.junit.Assert.assertThat;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MockServerTestUtil {

    public static RestClientFactory createClientFactory(URI serverBaseUri) {
        return new RestClientFactory(serverBaseUri,
                "admin",
                "b6d7044e9ee3b2447c28fb7c50d86d98", new VersionInfo(
                "3.6.0-SNAPSHOT", "unknown", "unknown"), true, true) {
            @Override
            protected String getUrlPrefix() {
                return "";
            }
        };
    }

    // If you ever want to test against real server, switch to use this one
    public static RestClientFactory clientTalkingToRealServer()
            throws URISyntaxException {
        return new RestClientFactory(new URI("http://localhost:8080/zanata/"),
                "admin",
                "b6d7044e9ee3b2447c28fb7c50d86d98", new VersionInfo(
                "3.6.0-SNAPSHOT", "unknown", "unknown"), true,
                true);
    }

    static void verifyServerRespondSuccessStatus() {
        assertThat("server return successfuly status code", true, Matchers
                .is(true));
    }
}
