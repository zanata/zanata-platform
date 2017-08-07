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
package org.zanata.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.infinispan.manager.CacheContainer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.ContextController;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.exception.InvalidDateParamException;
import org.zanata.jpa.FullText;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.contribution.BaseContributionStatistic;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.rest.dto.stats.contribution.LocaleStatistics;
import org.zanata.service.ValidationService;
import org.zanata.service.impl.TranslationStateCacheImpl;
import org.zanata.service.impl.TranslationStateCacheImpl.DocumentStatisticLoader;
import org.zanata.service.impl.TranslationStateCacheImpl.HTextFlowTargetIdLoader;
import org.zanata.service.impl.TranslationStateCacheImpl.HTextFlowTargetValidationLoader;
import org.zanata.service.impl.VersionStateCacheImpl.VersionStatisticLoader;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.Zanata;

import com.google.common.collect.Lists;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ TranslationStateCacheImpl.class,
        DocumentStatisticLoader.class, HTextFlowTargetIdLoader.class,
        HTextFlowTargetValidationLoader.class, VersionStatisticLoader.class })
@SupportDeltaspikeCore
public class StatisticsServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    private StatisticsServiceImpl statisticsService;

    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private ContextController contextController;

    @Produces @Mock ValidationService validationService;
    @Produces @Mock @FullText FullTextEntityManager fullTextEntityManager;

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Produces @Zanata
    CacheContainer getCacheContainer() {
        return new InfinispanTestCacheContainer();
    }

    @Produces @Mock
    private TextFlowDAO textFlowDAO;

    private final SimpleDateFormat formatter =
            new SimpleDateFormat(StatisticsResource.DATE_FORMAT);

    private Date today = new Date();

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void initializeRequestScope() {
        // NB: This is easier than adding @InRequestScope to all test methods
        contextController.openRequest();
    }

    @Test
    public void getSimpleIterationStatisticsForAllLocales() {
        ContainerTranslationStatistics stats =
                statisticsService.getStatistics("sample-project", "1.0", false,
                        false, new String[] {});

        // Make sure the id matches
        assertThat(stats.getId()).isEqualTo("1.0");
        // Make sure there are links
        assertThat(stats.getRefs().size()).isGreaterThan(0);

        // No detailed stats
        assertThat(stats.getDetailedStats()).isNull();

        assertThat(stats.getStats().get(0).getUnit()).isEqualTo(
                TranslationStatistics.StatUnit.MESSAGE);

        for (TranslationStatistics transStat : stats.getStats()) {
            // Check that there are no word level stats
            assertThat(transStat.getUnit()).isNotEqualTo(
                    TranslationStatistics.StatUnit.WORD);

            // make sure counts are sane
            assertThat(
                    transStat.getDraft() + transStat.getApproved()
                            + transStat.getTranslatedOnly()
                            + transStat.getUntranslated()).isEqualTo(
                    transStat.getTotal());
        }
    }

    @Test
    public void getWordIterationStatisticsForAllLocales() {
        ContainerTranslationStatistics stats =
                statisticsService.getStatistics("sample-project", "1.0", false,
                        true, new String[] {});

        // Make sure the id matches
        assertThat(stats.getId()).isEqualTo("1.0");
        // Make sure there are links
        assertThat(stats.getRefs().size()).isGreaterThan(0);

        // No detailed stats
        assertThat(stats.getDetailedStats()).isNull();

        // Word level AND message level stats
        int wordLevel = 0;
        int mssgLevel = 0;
        for (TranslationStatistics transStat : stats.getStats()) {
            if (transStat.getUnit() == TranslationStatistics.StatUnit.MESSAGE) {
                mssgLevel++;
            } else if (transStat.getUnit() == TranslationStatistics.StatUnit.WORD) {
                wordLevel++;
            }

            // make sure counts are sane
            assertThat(
                    transStat.getDraft() + transStat.getApproved()
                            + transStat.getTranslatedOnly()
                            + transStat.getUntranslated()).isEqualTo(
                    transStat.getTotal());
        }

        // make sure word and message level counts are the same and > 0
        assertThat(wordLevel).isGreaterThan(0);
        assertThat(wordLevel).isEqualTo(mssgLevel);
    }

    @Test
    public void getDetailedIterationStatisticsForSpecificLocales() {
        String[] locales = new String[] { "en-US", "es", "as" };

        ContainerTranslationStatistics stats =
                statisticsService.getStatistics("sample-project", "1.0", true,
                        true, locales);

        // Make sure the id matches
        assertThat(stats.getId()).isEqualTo("1.0");
        // Make sure there are links
        assertThat(stats.getRefs().size()).isGreaterThan(0);

        // Detailed Stats
        assertThat(stats.getDetailedStats().size()).isGreaterThan(0);

        // Results returned only for specified locales
        for (TranslationStatistics transStat : stats.getStats()) {
            assertThat(Arrays.asList(locales)).contains(transStat.getLocale());
            // make sure counts are sane
            assertThat(
                    transStat.getDraft() + transStat.getApproved()
                            + transStat.getTranslatedOnly()
                            + transStat.getUntranslated()).isEqualTo(
                    transStat.getTotal());
        }
    }

    @Test
    public void getSimpleDocumentStatisticsForAllLocales() {
        ContainerTranslationStatistics stats =
                statisticsService.getStatistics("sample-project", "1.0",
                        "my/path/document.txt", false, new String[] {});

        // Make sure the id matches
        assertThat(stats.getId()).isEqualTo("my/path/document.txt");
        // Make sure there are links
        assertThat(stats.getRefs().size()).isGreaterThan(0);

        // No detailed stats
        assertThat(stats.getDetailedStats()).isNull();

        assertThat(stats.getStats().get(0).getUnit()).isEqualTo(
                TranslationStatistics.StatUnit.MESSAGE);

        for (TranslationStatistics transStat : stats.getStats()) {
            // Check that there are no word level stats
            assertThat(transStat.getUnit()).isNotEqualTo(
                    TranslationStatistics.StatUnit.WORD);

            // make sure counts are sane
            assertThat(
                    transStat.getDraft() + transStat.getApproved()
                            + transStat.getTranslatedOnly()
                            + transStat.getUntranslated()).isEqualTo(
                    transStat.getTotal());
        }
    }

    @Test
    public void getDetailedDocumentStatisticsForSpecificLocales() {
        String[] locales = new String[] { "en-US", "es", "as" };

        ContainerTranslationStatistics stats =
                statisticsService.getStatistics("sample-project", "1.0",
                        "my/path/document.txt", true, locales);

        // Make sure the id matches
        assertThat(stats.getId()).isEqualTo("my/path/document.txt");
        // Make sure there are links
        assertThat(stats.getRefs().size()).isGreaterThan(0);

        // No Detailed Stats
        assertThat(stats.getDetailedStats()).isNull();

        // Results returned only for specified locales
        for (TranslationStatistics transStat : stats.getStats()) {
            assertThat(Arrays.asList(locales)).contains(transStat.getLocale());
            // make sure counts are sane
            assertThat(
                    transStat.getDraft() + transStat.getApproved()
                            + transStat.getTranslatedOnly()
                            + transStat.getUntranslated()).isEqualTo(
                    transStat.getTotal());
        }
    }

    @Test(expected = NoSuchEntityException.class)
    public void contributionStatsInvalidVersion() {
        statisticsService.getContributionStatistics("non-exist-project",
                "non-exist-version", "admin", "2013-01-01..2014-01-01", false);
    }

    @Test(expected = NoSuchEntityException.class)
    public void contributionStatsInvalidPerson() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "non-exist-user", "2013-01-01..2014-01-01", false);
    }

    @Test(expected = InvalidDateParamException.class)
//            description = "invalid date range separator")
    public void contributionStatsInvalidDateRange1() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "admin", "2013-01-012014-01-01", false);
    }

    @Test(expected = InvalidDateParamException.class)
//            description = "invalid date format")
    public void contributionStatsInvalidDateRange2() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "admin", "203-01-01..201-01-01", false);
    }

    @Test(expected = InvalidDateParamException.class)
//            description = "toDate is before fromDate")
    public void contributionStatsInvalidDateRange3() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "admin", "2014-01-01..2013-01-01", false);
    }

    @Test(expected = InvalidDateParamException.class)
//            description = "date range is more than max-range(365 days)")
    public void contributionStatsInvalidDateRange4() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "admin", "2013-01-01..2014-06-01", false);
    }

    @Test
    public void getContribStatsSingleTarget() {
        // Initial state = needReview
        HTextFlowTarget target = textFlowTargetDAO.findById(2L);

        int wordCount = target.getTextFlow().getWordCount().intValue();

        String todayDate = formatter.format(today);

        String username = "demo";
        HPerson demoPerson = personDAO.findByUsername(username);

        ContributionStatistics initialStats =
                statisticsService.getContributionStatistics(
                        "sample-project", "1.0", username, todayDate + ".."
                                + todayDate, false);

        BaseContributionStatistic translationStats = getLocaleTranslationStats(
                target.getLocaleId(), initialStats.getContributions());

        // Should have no stats for user on today
        assertNull(translationStats);

        // needReview -> approved
        ContributionStatistics expectedStats = new ContributionStatistics(
                username,
                Lists.newArrayList(buildStats(target.getLocaleId(), 0, 0,
                        wordCount, 0)));
        target = executeStateChangeTest(target, "test1", ContentState.Approved,
                demoPerson, expectedStats);

        // approved -> approved
        target = executeStateChangeTest(target, "test2", ContentState.Approved,
                demoPerson, expectedStats);

        // approved -> approved
        target = executeStateChangeTest(target, "test3", ContentState.Approved,
                demoPerson, expectedStats);

        // approved -> needReview
        expectedStats = new ContributionStatistics(
                username,
                Lists.newArrayList(buildStats(target.getLocaleId(), wordCount,
                        0, 0, 0)));
        target = executeStateChangeTest(target, "test4",
                ContentState.NeedReview, demoPerson, expectedStats);
    }

    @Test
    public void getContribStatsSameLocaleMultiTargets() {
        String username = "demo";
        HPerson demoPerson = personDAO.findByUsername(username);

        // Initial state = new (en-us)
        HTextFlowTarget target1 = textFlowTargetDAO.findById(5L);

        // Initial state = new (en-us)
        HTextFlowTarget target2 = textFlowTargetDAO.findById(6L);

        LocaleId localeId = target1.getLocaleId(); // same as target2

        int wordCount1 = target1.getTextFlow().getWordCount().intValue();
        int wordCount2 = target2.getTextFlow().getWordCount().intValue();

        // new -> approved
        ContentState newState = ContentState.Approved;

        ContributionStatistics expectedStats =
                new ContributionStatistics(username,
                        Lists.newArrayList(
                                buildStats(localeId, 0, 0, wordCount1, 0)));
        target1 = executeStateChangeTest(target1, "test1",
                newState, demoPerson, expectedStats);

        expectedStats =
                new ContributionStatistics(username,
                        Lists.newArrayList(
                                buildStats(localeId, 0, 0,
                                        wordCount1 + wordCount2, 0)));

        target2 = executeStateChangeTest(target2, "test1",
                newState, demoPerson, expectedStats);

        // approved -> needReview
        newState = ContentState.NeedReview;

        expectedStats =
                new ContributionStatistics(username,
                        Lists.newArrayList(
                                buildStats(localeId, wordCount1, 0,
                                        wordCount1, 0)));
        target1 = executeStateChangeTest(target1, "test2",
                newState, demoPerson, expectedStats);

        expectedStats =
                new ContributionStatistics(username,
                        Lists.newArrayList(
                                buildStats(localeId, wordCount1 + wordCount2,
                                        0, 0, 0)));
        target2 = executeStateChangeTest(target2, "test2",
                newState, demoPerson, expectedStats);
    }

    @Test
    public void getContributionStatisticsMultiLocale() {
        String username = "demo";
        HPerson demoPerson = personDAO.findByUsername(username);

        // Initial state = needReview (AS)
        HTextFlowTarget target1 = textFlowTargetDAO.findById(1L);

        // Initial state = needReview (DE)
        HTextFlowTarget target2 = textFlowTargetDAO.findById(2L);

        int wordCount1 = target1.getTextFlow().getWordCount().intValue();
        int wordCount2 = target2.getTextFlow().getWordCount().intValue();

        // needReview -> approved
        ContentState newState = ContentState.Approved;

        ContributionStatistics expectedStats = new ContributionStatistics(
                username,
                Lists.newArrayList(buildStats(target1.getLocaleId(), 0, 0,
                        wordCount1, 0)));
        target1 = executeStateChangeTest(target1, "test1",
                newState, demoPerson, expectedStats);

        expectedStats.getContributions()
                .add(buildStats(target2.getLocaleId(), 0,
                        0, wordCount2, 0));
        target2 = executeStateChangeTest(target2, "test1",
                newState, demoPerson, expectedStats);

        // approved -> needReview
        newState = ContentState.NeedReview;
        BaseContributionStatistic localeStat =
                getLocaleTranslationStats(target1.getLocaleId(),
            expectedStats.getContributions());
        localeStat.set(newState, localeStat.get(newState) + wordCount1);
        localeStat.set(ContentState.Approved, 0);
        target1 = executeStateChangeTest(target1, "test2",
                newState, demoPerson, expectedStats);
        localeStat = getLocaleTranslationStats(target2.getLocaleId(),
                expectedStats.getContributions());
        localeStat.set(newState, localeStat.get(newState) + wordCount2);
        localeStat.set(ContentState.Approved, 0);
        target2 = executeStateChangeTest(target2, "test2",
                newState, demoPerson, expectedStats);

    }

    private BaseContributionStatistic getLocaleTranslationStats(
            LocaleId localeId, List<LocaleStatistics> statisticList) {
        for (LocaleStatistics stats : statisticList) {
            if (stats.getLocale().equals(localeId)) {
                return stats.getTranslationStats();
            }
        }
        return null;
    }

    @Test
    public void getContribStatsDiffUser() {
        String username1 = "demo";
        String username2 = "admin";
        HPerson person1 = personDAO.findByUsername(username1);
        HPerson person2 = personDAO.findByUsername(username2);

        HTextFlowTarget target = textFlowTargetDAO.findById(1L);

        int wordCount = target.getTextFlow().getWordCount().intValue();

        ContentState newState = ContentState.Approved;

        ContributionStatistics expectedStats = new ContributionStatistics(
                username1,
                Lists.newArrayList(buildStats(target.getLocaleId(), 0, 0,
                        wordCount, 0)));
        target = executeStateChangeTest(target, "test1",
                newState, person1, expectedStats);

        ContributionStatistics expectedStats2 = new ContributionStatistics(
                username2,
                Lists.newArrayList(buildStats(target.getLocaleId(), 0, 0,
                        wordCount, 0)));
        target = executeStateChangeTest(target, "test2",
                newState, person2, expectedStats2);

        //Test person1 statistic has not changed
        String todayDate = formatter.format(today);
        ContributionStatistics newStats =
                statisticsService.getContributionStatistics(
                        "sample-project", "1.0", person1.getAccount()
                                .getUsername(), todayDate + ".." + todayDate, false);
        assertThat(newStats).isEqualTo(expectedStats);
    }

    private LocaleStatistics buildStats(LocaleId localeId, int needReview,
            int translated, int approved, int rejected) {
        BaseContributionStatistic translationStats =
                new BaseContributionStatistic(approved,
                        needReview, translated, rejected);
        return new LocaleStatistics(localeId, translationStats, null);
    }

    private HTextFlowTarget executeStateChangeTest(HTextFlowTarget target,
            String newContent, ContentState newState, HPerson translator,
            ContributionStatistics expectedStats) {
        target = changeState(target, newContent, newState, translator);

        String todayDate = formatter.format(today);

        ContributionStatistics newStats =
                statisticsService.getContributionStatistics(
                        "sample-project", "1.0", translator.getAccount()
                                .getUsername(), todayDate + ".." + todayDate, false);

        assertNotNull(newStats);
        assertThat(newStats.getUsername())
                .isEqualTo(expectedStats.getUsername());
        assertThat(newStats.getContributions())
                .containsAll(expectedStats.getContributions());
        return target;
    }

    private HTextFlowTarget changeState(HTextFlowTarget target,
            String contents, ContentState newState, HPerson translator) {
        target.setContents(contents);
        target.setState(newState);
        target.setTranslator(translator);

        textFlowTargetDAO.makePersistent(target);
        textFlowTargetDAO.flush();

        return textFlowTargetDAO.findById(target.getId());
    }
}
