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

import java.util.List;

import org.zanata.rest.dto.LocaleDetails;

import com.sun.jersey.api.client.GenericType;

/**
 * REST client for project iteration locales.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ProjectIterationLocalesClient {
    private final RestClientFactory restClientFactory;
    private final String projectSlug;
    private final String versionSlug;

    public ProjectIterationLocalesClient(RestClientFactory restClientFactory,
            String projectSlug, String versionSlug) {
        this.restClientFactory = restClientFactory;
        this.projectSlug = projectSlug;
        this.versionSlug = versionSlug;
    }

    public List<LocaleDetails> getLocales() {
        return restClientFactory.getClient()
                .resource(restClientFactory.getBaseUri())
                .path("projects").path("p").path(projectSlug)
                .path("iterations").path("i").path(versionSlug)
                .path("locales")
                .get(new GenericType<List<LocaleDetails>>() {
                });
    }
}
