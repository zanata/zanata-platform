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

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.rest.service.StubbingServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class StatisticsResourceClientTest {
    @ClassRule
    public static StubbingServerRule
            stubbingServerRule = new StubbingServerRule();

    private StatisticsResourceClient client;

    @Before
    public void setUp() throws URISyntaxException {
        client =
                new StatisticsResourceClient(
                        MockServerTestUtil.createClientFactory(
                                stubbingServerRule.getServerBaseUri()));
    }

    @Test
    public void testGetIterationStatistics() {
        String versionSlug = "master";
        ContainerTranslationStatistics statistics =
                client.getStatistics("pahuang-test", versionSlug, true, true,
                        new String[] { "de-DE", "de", "zh-CN" });

        assertThat(statistics.getId()).isEqualTo(versionSlug);
        assertThat(statistics.getStats()).hasSize(3);
    }

    @Deprecated
    @Test
    public void testGetDocStatistics() {
        String docId = "About-Fedora";
        ContainerTranslationStatistics statistics =
                client.getStatistics("about-fedora", "master", docId,
                        true, new String[] { "de-DE", "zh-CN" });
        assertThat(statistics.getId()).isEqualTo(docId);
        assertThat(statistics.getStats()).hasSize(2);
    }

    @Test
    public void testGetDocStatisticsWithDocId() {
        String docId = "About-Fedora";
        ContainerTranslationStatistics statistics =
                client.getStatisticsWithDocId("about-fedora", "master", docId,
                        true, new String[] { "de-DE", "zh-CN" });
        assertThat(statistics.getId()).isEqualTo(docId);
        assertThat(statistics.getStats()).hasSize(2);
    }

    @Test
    public void testGetContributorStatistics() {
        ContributionStatistics statistics =
                client.getContributionStatistics("about-fedora", "master",
                        "pahuang", "2014-10-01..2014-11-10", false);
        assertThat(statistics.getUsername()).isEqualTo("pahuang");
    }
}


