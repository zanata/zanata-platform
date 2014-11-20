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
package org.zanata.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import net.sf.ehcache.CacheManager;

import org.dbunit.operation.DatabaseOperation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.exception.InvalidDateParamException;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.contribution.BaseContributionStatistic;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.rest.dto.stats.contribution.LocaleStatistics;
import org.zanata.rest.service.StatisticsResource;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.ValidationService;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Test(groups = { "business-tests" })
public class StatisticsServiceImplTest extends ZanataDbunitJpaTest {
    private SeamAutowire seam = SeamAutowire.instance();

    @Mock
    private ValidationService validationServiceImpl;
    private CacheManager cacheManager;

    private StatisticsServiceImpl statisticsService;

    private TextFlowTargetDAO textFlowTargetDAO;

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

    @BeforeMethod
    public void initializeSeam() {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
      seam.reset()
            .use("entityManager", getEm())
            .use("session", getSession())
            .use("validationServiceImpl", validationServiceImpl)
            .useImpl(TranslationStateCacheImpl.class)
            .ignoreNonResolvable();
      // @formatter:on
        cacheManager = CacheManager.create();
        cacheManager.removalAll();

        statisticsService = seam.autowire(StatisticsServiceImpl.class);
        textFlowTargetDAO = seam.autowire(TextFlowTargetDAO.class);
    }

    @AfterMethod
    public void after() {
        cacheManager.shutdown();
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

    @Test(expectedExceptions = NoSuchEntityException.class)
    public void contributionStatsInvalidVersion() {
        statisticsService.getContributionStatistics("non-exist-project",
                "non-exist-version", "admin", "2013-01-01..2014-01-01");
    }

    @Test(expectedExceptions = NoSuchEntityException.class)
    public void contributionStatsInvalidPerson() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "non-exist-user", "2013-01-01..2014-01-01");
    }

    @Test(expectedExceptions = InvalidDateParamException.class,
            description = "invalid date range separator")
    public void contributionStatsInvalidDateRange1() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "admin", "2013-01-012014-01-01");
    }

    @Test(expectedExceptions = InvalidDateParamException.class,
            description = "invalid date format")
    public void contributionStatsInvalidDateRange2() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "admin", "203-01-01..201-01-01");
    }

    @Test(expectedExceptions = InvalidDateParamException.class,
            description = "toDate is before fromDate")
    public void contributionStatsInvalidDateRange3() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "admin", "2014-01-01..2013-01-01");
    }

    @Test(expectedExceptions = InvalidDateParamException.class,
            description = "date range is more than max-range(365 days)")
    public void contributionStatsInvalidDateRange4() {
        statisticsService.getContributionStatistics("sample-project",
                "1.0", "admin", "2013-01-01..2014-06-01");
    }

    @Test
    public void getContribStatsSingleTarget() {
        PersonDAO personDAO = seam.autowire(PersonDAO.class);

        // Initial state = needReview
        HTextFlowTarget target = textFlowTargetDAO.findById(2L);

        int wordCount = target.getTextFlow().getWordCount().intValue();

        String todayDate = formatter.format(today);

        String username = "demo";
        HPerson demoPerson = personDAO.findByUsername(username);

        ContributionStatistics initialStats =
                statisticsService.getContributionStatistics(
                        "sample-project", "1.0", username, todayDate + ".."
                                + todayDate);

        BaseContributionStatistic stats =
                initialStats.get(username).get(target.getLocaleId());

        // Should have no stats for user on today
        assertNull(stats);

        // needReview -> approved
        ContributionStatistics expectedStats = new ContributionStatistics();
        expectedStats.put(username, buildStats(target.getLocaleId(), 0, 0,
                wordCount, 0));
        target = executeStateChangeTest(target, "test1", ContentState.Approved,
                demoPerson, expectedStats);

        // approved -> approved
        target = executeStateChangeTest(target, "test2", ContentState.Approved,
                demoPerson, expectedStats);

        // approved -> approved
        target = executeStateChangeTest(target, "test3", ContentState.Approved,
                demoPerson, expectedStats);

        // approved -> needReview
        expectedStats.put(username, buildStats(target.getLocaleId(), wordCount,
                0, 0, 0));
        target = executeStateChangeTest(target, "test4",
                ContentState.NeedReview, demoPerson, expectedStats);
    }

    @Test
    public void getContribStatsSameLocaleMultiTargets() {
        PersonDAO personDAO = seam.autowire(PersonDAO.class);
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

        ContributionStatistics expectedStats = new ContributionStatistics();
        expectedStats.put(username, buildStats(localeId, 0, 0,
                wordCount1, 0));
        target1 = executeStateChangeTest(target1, "test1",
                newState, demoPerson, expectedStats);

        expectedStats.put(username, buildStats(localeId, 0, 0,
                wordCount1 + wordCount2, 0));
        target2 = executeStateChangeTest(target2, "test1",
                newState, demoPerson, expectedStats);

        // approved -> needReview
        newState = ContentState.NeedReview;
        expectedStats.put(username, buildStats(localeId, wordCount1, 0,
                wordCount1, 0));
        target1 = executeStateChangeTest(target1, "test2",
                newState, demoPerson, expectedStats);

        expectedStats.put(username, buildStats(localeId, wordCount1 + wordCount2,
                0, 0, 0));
        target2 = executeStateChangeTest(target2, "test2",
                newState, demoPerson, expectedStats);
    }

    @Test
    public void getContributionStatisticsMultiLocale() {
        PersonDAO personDAO = seam.autowire(PersonDAO.class);
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

        ContributionStatistics expectedStats = new ContributionStatistics();
        expectedStats.put(username, buildStats(target1.getLocaleId(), 0, 0,
                wordCount1, 0));
        target1 = executeStateChangeTest(target1, "test1",
                newState, demoPerson, expectedStats);

        expectedStats.get(username).putAll(buildStats(target2.getLocaleId(), 0,
                0, wordCount2, 0));
        target2 = executeStateChangeTest(target2, "test1",
                newState, demoPerson, expectedStats);

        // approved -> needReview
        newState = ContentState.NeedReview;
        BaseContributionStatistic localeStat = expectedStats.get(username)
                .get(target1.getLocaleId());
        localeStat.set(newState, localeStat.get(newState) + wordCount1);
        localeStat.set(ContentState.Approved, 0);
        target1 = executeStateChangeTest(target1, "test2",
                newState, demoPerson, expectedStats);

        localeStat = expectedStats.get(username).get(target2.getLocaleId());
        localeStat.set(newState, localeStat.get(newState) + wordCount2);
        localeStat.set(ContentState.Approved, 0);
        target2 = executeStateChangeTest(target2, "test2",
                newState, demoPerson, expectedStats);

    }

    @Test
    public void getContribStatsDiffUser() {
        PersonDAO personDAO = seam.autowire(PersonDAO.class);

        String username1 = "demo";
        String username2 = "admin";
        HPerson person1 = personDAO.findByUsername(username1);
        HPerson person2 = personDAO.findByUsername(username2);

        HTextFlowTarget target = textFlowTargetDAO.findById(1L);

        int wordCount = target.getTextFlow().getWordCount().intValue();

        ContentState newState = ContentState.Approved;

        ContributionStatistics expectedStats = new ContributionStatistics();
        expectedStats.put(username1, buildStats(target.getLocaleId(), 0, 0,
                wordCount, 0));
        target = executeStateChangeTest(target, "test1",
                newState, person1, expectedStats);

        ContributionStatistics expectedStats2 = new ContributionStatistics();
        expectedStats2.put(username2, buildStats(target.getLocaleId(), 0, 0,
                wordCount, 0));
        target = executeStateChangeTest(target, "test2",
                newState, person2, expectedStats2);

        //Test person1 statistic has not changed
        String todayDate = formatter.format(today);
        ContributionStatistics newStats =
                statisticsService.getContributionStatistics(
                        "sample-project", "1.0", person1.getAccount()
                                .getUsername(), todayDate + ".." + todayDate);
        assertThat(newStats).isEqualTo(expectedStats);
    }

    private LocaleStatistics buildStats(LocaleId localeId, int needReview,
            int translated, int approved, int rejected) {
        LocaleStatistics localeStatistics = new LocaleStatistics();

        localeStatistics.put(localeId, new BaseContributionStatistic(approved,
                needReview, translated, rejected));

        return localeStatistics;
    }

    private HTextFlowTarget executeStateChangeTest(HTextFlowTarget target,
            String newContent, ContentState newState, HPerson translator,
            ContributionStatistics expectedStats) {
        target = changeState(target, newContent, newState, translator);

        String todayDate = formatter.format(today);

        ContributionStatistics newStats =
                statisticsService.getContributionStatistics(
                        "sample-project", "1.0", translator.getAccount()
                                .getUsername(), todayDate + ".." + todayDate);

        assertNotNull(newStats);
        assertThat(newStats).isEqualTo(expectedStats);
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
