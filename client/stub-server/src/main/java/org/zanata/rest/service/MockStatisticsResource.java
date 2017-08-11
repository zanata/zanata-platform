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

package org.zanata.rest.service;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.contribution.BaseContributionStatistic;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.rest.dto.stats.contribution.LocaleStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path(StatisticsResource.SERVICE_PATH)
public class MockStatisticsResource implements StatisticsResource {

    private static final long serialVersionUID = -1270337376508171813L;

    @Override
    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug,
            @DefaultValue("false") boolean includeDetails,
            @DefaultValue("false") boolean includeWordStats, String[] locales) {
        return generateStatistics(iterationSlug, locales);
    }

    private ContainerTranslationStatistics generateStatistics(
            String id, String[] locales) {
        ContainerTranslationStatistics stats =
                new ContainerTranslationStatistics();
        stats.setId(id);
        for (String locale : locales) {
            stats.addStats(new TranslationStatistics(new TransUnitCount(
                    100, 0, 0, 100, 0), locale.trim()));
        }
        return stats;
    }

    @Override
    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug, String docId,
            @DefaultValue("false") boolean includeWordStats, String[] locales) {
        return getStatisticsWithDocId(projectSlug, iterationSlug, docId,
                includeWordStats, locales);
    }

    @Override
    public ContainerTranslationStatistics getStatisticsWithDocId(
            String projectSlug, String iterationSlug, String docId,
            boolean includeWordStats, String[] locales) {
        return generateStatistics(docId, locales);
    }

    @Override
    public ContributionStatistics getContributionStatistics(String projectSlug,
            String versionSlug, String username, String dateRange, boolean includeAutomatedEntry) {

        BaseContributionStatistic transStats = new BaseContributionStatistic(100, 90, 100, 0);
        BaseContributionStatistic reviewStats = new BaseContributionStatistic(100, 0, 0, 10);

        LocaleStatistics localeStatistics =
            new LocaleStatistics(new LocaleId("zh"), transStats, reviewStats);

        List<LocaleStatistics> localeStatisticsList = new ArrayList<>();
        localeStatisticsList.add(localeStatistics);

        ContributionStatistics contributionStatistics =
            new ContributionStatistics(username, localeStatisticsList);

        return contributionStatistics;
    }

    @Override
    public Response getProjectStatisticsMatrix(String projectSlug,
            String versionSlug, String dateRangeParam, String timeZoneID) {
        return MockResourceUtil.notUsedByClient();
    }
}

