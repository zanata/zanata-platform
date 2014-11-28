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
package org.zanata.util;

import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SampleDataResourceClient {

    private static void checkAndReleaseConnection(Response response1) {
        ClientResponse response = (ClientResponse) response1;
        assertThat(response.getStatus(), Matchers.equalTo(200));
        response.releaseConnection();
    }

    private static ClientRequest createRequest(String path) {
        ClientRequest clientRequest =
                new ClientRequest(
                        PropertiesHolder.getProperty(Constants.zanataInstance
                                .value()) + "rest/test/data/sample" + path);
        // having null username will bypass ZanataRestSecurityInterceptor
        // clientRequest.header("X-Auth-User", null);
        clientRequest.getHeaders().remove("X-Auth-User");
        clientRequest.header("X-Auth-Token",
                PropertiesHolder.getProperty(Constants.zanataApiKey.value()));
        clientRequest.header("Content-Type", "application/xml");
        return clientRequest;
    }

    public static void deleteExceptEssentialData() throws Exception {
        checkAndReleaseConnection(createRequest("").delete());
    }

    public static void makeSampleUsers() throws Exception {
        checkAndReleaseConnection(createRequest("/users").put());
    }

    /**
     * @param username
     *            username to join language team
     * @param localesCSV
     *            locale ids separated by comma
     */
    public static void userJoinsLanguageTeam(String username,
            String localesCSV) throws Exception {
        ClientRequest request =
                createRequest("/accounts/u/" + username + "/languages");

        checkAndReleaseConnection(request.queryParameter("locales", localesCSV)
                .put());
    }

    public static void makeSampleProject() throws Exception {
        checkAndReleaseConnection(createRequest("/project").put());
    }

    public static void makeSampleLanguages() throws Exception {
        checkAndReleaseConnection(createRequest("/languages").put());
    }

    public static void addLanguage(String localeId) throws Exception {
        checkAndReleaseConnection(createRequest("/languages/l/" + localeId)
                .put());
    }

}
