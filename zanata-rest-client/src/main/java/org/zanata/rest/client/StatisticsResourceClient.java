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
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DefaultValue;

import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.rest.service.StatisticsResource;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.WebResource;

import static org.zanata.rest.client.ClientUtil.asMultivaluedMap;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class StatisticsResourceClient implements StatisticsResource {
    private final RestClientFactory factory;
    private final URI baseUri;

    StatisticsResourceClient(RestClientFactory factory) {
        this.factory = factory;
        baseUri = factory.getBaseUri();
    }

    @Override
    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug,
            @DefaultValue("false") boolean includeDetails,
            @DefaultValue("false") boolean includeWordStats, String[] locales) {
        WebResource webResource =
                factory.getClient().resource(baseUri).path("stats")
                        .path("proj")
                        .path(projectSlug)
                        .path("iter")
                        .path(iterationSlug)
                        .queryParam("detail", String.valueOf(includeDetails))
                        .queryParam("word", String.valueOf(includeWordStats))
                        .queryParams(asMultivaluedMap("locale",
                                toLocaleList(locales)));
        return webResource.get(ContainerTranslationStatistics.class);
    }

    private static List<String> toLocaleList(String[] locales) {
        List<String> localesList;
        if (locales == null) {
            localesList = Lists.newArrayList();
        } else {
            localesList = Lists.newArrayList(locales);
        }
        return localesList;
    }

    @Override
    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug, String docId,
            @DefaultValue("false") boolean includeWordStats, String[] locales) {
        WebResource webResource =
                factory.getClient().resource(baseUri).path("stats")
                        .path("proj")
                        .path(projectSlug)
                        .path("iter")
                        .path(iterationSlug)
                        .path("doc")
                        .path(docId)
                        .queryParam("word", String.valueOf(includeWordStats))
                        .queryParams(asMultivaluedMap("locale",
                                toLocaleList(locales)));
        return webResource.get(ContainerTranslationStatistics.class);
    }

    @Override
    public ContributionStatistics getContributionStatistics(String projectSlug,
            String versionSlug, String username, String dateRange, boolean includeAutomatedEntry) {
        WebResource webResource =
                factory.getClient().resource(baseUri).path("stats")
                        .path("project")
                        .path(projectSlug)
                        .path("version")
                        .path(versionSlug)
                        .path("contributor")
                        .path(username)
                        .path(dateRange)
                        .queryParam("includeAutomatedEntry", String.valueOf(includeAutomatedEntry));
        return webResource.get(ContributionStatistics.class);
    }
}
