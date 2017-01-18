/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.compat;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Before;
import org.junit.Test;
import org.zanata.apicompat.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.apicompat.rest.dto.stats.TranslationStatistics;
import org.zanata.apicompat.rest.service.StatisticsResource;
import org.zanata.provider.DBUnitProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class StatisticsCompatibilityITCase extends CompatibilityBase {

    private StatisticsResource statsResource;

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/DocumentsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/HistoryTestData.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Before
    public void before() {
        statsResource = getStatisticsResource();
    }

    @Test
    @RunAsClient
    public void getStatisticsForIteration() throws Exception {
        ContainerTranslationStatistics stats =
                statsResource.getStatistics("sample-project", "1.0", true,
                        true, null);

        int wordStatCount = 0;
        int mssgStatCount = 0;

        assertThat(stats.getStats().size(), greaterThan(0));
        assertThat(stats.getDetailedStats().size(), greaterThan(0)); // Has
                                                                     // document
                                                                     // stats
        for (TranslationStatistics langStats : stats.getStats()) {
            assertThat(
                    langStats.getTotal(),
                    equalTo(langStats.getUntranslated()
                            + langStats.getDraft()
                            + langStats.getTranslatedAndApproved()));
            assertThat(langStats.getTotal(),
                    equalTo(langStats.getUntranslated() + langStats.getDraft()
                            + langStats.getTranslatedAndApproved()));
            assertThat(
                    langStats.getTotal(),
                    equalTo(langStats.getUntranslated() + langStats.getFuzzy()
                            + langStats.getRejected()
                            + langStats.getTranslatedOnly()
                            + langStats.getApproved()));
            // MatcherAssert.assertThat( 100,
            // Matchers.equalTo( asStats.getPercentNeedReview() +
            // asStats.getPercentUntranslated() + asStats.getPercentTranslated()
            // ));
            wordStatCount +=
                    (langStats.getUnit() == TranslationStatistics.StatUnit.WORD ? 1
                            : 0);
            mssgStatCount +=
                    (langStats.getUnit() == TranslationStatistics.StatUnit.MESSAGE ? 1
                            : 0);
        }

        assertThat("Word and Message level stat count should match",
                wordStatCount == mssgStatCount);
    }

    @Test
    @RunAsClient
    public void getStatisticsForDocument() throws Exception {
        ContainerTranslationStatistics stats =
                statsResource.getStatistics("sample-project", "1.0",
                        "my/path/document.txt", true, null);

        int wordStatCount = 0;
        int mssgStatCount = 0;

        assertThat(stats.getStats().size(), greaterThan(0));
        for (TranslationStatistics langStats : stats.getStats()) {
            assertThat(
                    langStats.getTotal(),
                    equalTo(langStats.getUntranslated()
                            + langStats.getDraft()
                            + langStats.getTranslatedAndApproved()));
            assertThat(langStats.getTotal(),
                    equalTo(langStats.getUntranslated() + langStats.getDraft()
                            + langStats.getTranslatedAndApproved()));
            assertThat(
                    langStats.getTotal(),
                    equalTo(langStats.getUntranslated() + langStats.getFuzzy()
                            + langStats.getRejected()
                            + langStats.getTranslatedOnly()
                            + langStats.getApproved()));
            // MatcherAssert.assertThat( 100,
            // Matchers.equalTo( asStats.getPercentNeedReview() +
            // asStats.getPercentUntranslated() + asStats.getPercentTranslated()
            // ));
            wordStatCount +=
                    (langStats.getUnit() == TranslationStatistics.StatUnit.WORD ? 1
                            : 0);
            mssgStatCount +=
                    (langStats.getUnit() == TranslationStatistics.StatUnit.MESSAGE ? 1
                            : 0);
        }

        assertThat("Word and Message level stat count should match",
                wordStatCount == mssgStatCount);
    }

    @Test
    @RunAsClient
    public void getStatisticsForIterationAndLocale() throws Exception {
        ContainerTranslationStatistics stats =
                statsResource.getStatistics("sample-project", "1.0", true,
                        false, new String[] { "as" });

        assertThat(stats.getStats().size(), is(1)); // Just one locale and no
                                                    // word level stats
        assertThat(stats.getDetailedStats().size(), greaterThan(0)); // Has
                                                                     // document
                                                                     // stats
        for (TranslationStatistics langStats : stats.getStats()) {
            assertThat(
                    langStats.getTotal(),
                    equalTo(langStats.getUntranslated()
                            + langStats.getDraft()
                            + langStats.getTranslatedAndApproved()));
            assertThat(langStats.getTotal(),
                    equalTo(langStats.getUntranslated() + langStats.getDraft()
                            + langStats.getTranslatedAndApproved()));
            assertThat(
                    langStats.getTotal(),
                    equalTo(langStats.getUntranslated() + langStats.getFuzzy()
                            + langStats.getRejected()
                            + langStats.getTranslatedOnly()
                            + langStats.getApproved()));
            // MatcherAssert.assertThat( 100,
            // Matchers.equalTo( asStats.getPercentNeedReview() +
            // asStats.getPercentUntranslated() + asStats.getPercentTranslated()
            // ));
            assertThat("Shouldn't have returned word level stats",
                    langStats.getUnit(),
                    not(TranslationStatistics.StatUnit.WORD));
        }
    }

    @Test
    @RunAsClient
    public void getStatisticsForDocumentAndLocale() throws Exception {
        ContainerTranslationStatistics stats =
                statsResource.getStatistics("sample-project", "1.0",
                        "my/path/document.txt", true, new String[] { "as" });

        assertThat(stats.getStats().size(), is(2)); // Just one locale
        for (TranslationStatistics langStats : stats.getStats()) {
            assertThat(
                    langStats.getTotal(),
                    equalTo(langStats.getUntranslated()
                            + langStats.getDraft()
                            + langStats.getTranslatedAndApproved()));
            assertThat(langStats.getTotal(),
                    equalTo(langStats.getUntranslated() + langStats.getDraft()
                            + langStats.getTranslatedAndApproved()));
            assertThat(
                    langStats.getTotal(),
                    equalTo(langStats.getUntranslated() + langStats.getFuzzy()
                            + langStats.getRejected()
                            + langStats.getTranslatedOnly()
                            + langStats.getApproved()));
            // MatcherAssert.assertThat( 100,
            // Matchers.equalTo( asStats.getPercentNeedReview() +
            // asStats.getPercentUntranslated() + asStats.getPercentTranslated()
            // ));
        }
    }
}
