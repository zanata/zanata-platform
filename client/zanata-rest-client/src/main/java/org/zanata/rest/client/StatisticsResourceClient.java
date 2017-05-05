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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class StatisticsResourceClient {
    private final RestClientFactory factory;
    private final URI baseUri;

    StatisticsResourceClient(RestClientFactory factory) {
        this.factory = factory;
        baseUri = factory.getBaseUri();
    }

    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug,
            @DefaultValue("false") boolean includeDetails,
            @DefaultValue("false") boolean includeWordStats, String[] locales) {
        WebTarget webResource =
                factory.getClient().target(baseUri).path("stats")
                        .path("proj")
                        .path(projectSlug)
                        .path("iter")
                        .path(iterationSlug)
                        .queryParam("detail", includeDetails)
                        .queryParam("word", includeWordStats)
                        .queryParam("locale", (Object[]) locales);
        return webResource.request(MediaType.APPLICATION_XML_TYPE)
                .get(ContainerTranslationStatistics.class);
    }

    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug, String docId,
            @DefaultValue("false") boolean includeWordStats, String[] locales) {
        WebTarget webResource =
                factory.getClient().target(baseUri).path("stats")
                        .path("proj")
                        .path(projectSlug)
                        .path("iter")
                        .path(iterationSlug)
                        .path("doc")
                        .path(docId)
                        .queryParam("word", String.valueOf(includeWordStats))
                        .queryParam("locale", (Object[]) locales);
        return webResource.request(MediaType.APPLICATION_XML_TYPE)
                .get(ContainerTranslationStatistics.class);
    }

    public ContributionStatistics getContributionStatistics(String projectSlug,
            String versionSlug, String username, String dateRange, boolean includeAutomatedEntry) {
        WebTarget webResource =
                factory.getClient().target(baseUri).path("stats")
                        .path("project")
                        .path(projectSlug)
                        .path("version")
                        .path(versionSlug)
                        .path("contributor")
                        .path(username)
                        .path(dateRange)
                        .queryParam("includeAutomatedEntry", includeAutomatedEntry);
        return webResource.request(MediaType.APPLICATION_JSON_TYPE)
                .get(ContributionStatistics.class);
    }
}
