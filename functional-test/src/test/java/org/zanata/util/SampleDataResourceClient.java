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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SampleDataResourceClient {

    private static WebResource.Builder createRequest(String path) {
        WebResource.Builder resource =
                Client.create()
                        .resource(
                                PropertiesHolder
                                        .getProperty(Constants.zanataInstance
                                                .value())
                                        + "rest/test/data/sample" + path)
                        // having null username will bypass
                        // ZanataRestSecurityInterceptor
                        // clientRequest.header("X-Auth-User", null);
                        .header("X-Auth-Token",
                                PropertiesHolder
                                        .getProperty(Constants.zanataApiKey
                                                .value()))
                        .header("Content-Type", "application/xml");
        return resource;
    }

    public static void deleteExceptEssentialData() throws Exception {
        createRequest("").delete();
    }

    public static void makeSampleUsers() throws Exception {
        createRequest("/users").put();
    }

    /**
     * @param username
     *            username to join language team
     * @param localesCSV
     *            locale ids separated by comma
     */
    public static void userJoinsLanguageTeam(String username,
            String localesCSV) throws Exception {
        Client.create()
                .resource(
                        PropertiesHolder.getProperty(Constants.zanataInstance
                                .value()) + "rest/test/data/sample/accounts/u/"
                                + username + "/languages")
                .queryParam("locales", localesCSV)
                .header("X-Auth-Token",
                        PropertiesHolder.getProperty(Constants.zanataApiKey
                                .value()))
                .header("Content-Type", "application/xml").put();
    }

    public static void makeSampleProject() throws Exception {
        createRequest("/project").put();
    }

    public static void makeSampleLanguages() throws Exception {
        createRequest("/languages").put();
    }

    public static void addLanguage(String localeId) throws Exception {
        createRequest("/languages/l/" + localeId).put();
    }

}

