/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.infinispan.manager.CacheContainer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.cdi.TestTransaction;
import org.zanata.common.ContentState;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.VersionLocaleKey;
import org.zanata.test.CdiUnitRunner;
import org.zanata.transaction.TransactionUtil;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;
import org.zanata.util.TranslationUtil;
import org.zanata.util.Zanata;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ValidationId;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
@AdditionalClasses({
        LocaleServiceImpl.class,
        VersionStateCacheImpl.class,
        TranslationStateCacheImpl.class,
        // classes invoked via ServiceLocator (they won't get autowired by cdiunit
        TransactionUtil.class
})
public class MergeTranslationsServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    MergeTranslationsServiceImpl service;

    @Inject
    ProjectIterationDAO projectIterationDAO;

    @Inject
    TextFlowDAO textFlowDAO;

    @Inject
    TextFlowTargetDAO textFlowTargetDAO;

    @Produces @Mock
    private ZanataIdentity identity;

    @Produces @Mock @Authenticated
    HAccount authenticatedAccount;

    @Produces @Zanata
    CacheContainer cacheContainer = new InfinispanTestCacheContainer();

    @Produces
    IServiceLocator serviceLocator = spy(ServiceLocator.instance());

    @Produces @Mock @FullText
    FullTextEntityManager fullTextEntityManager;

    @Inject
    DocumentDAO documentDAO;

    @Produces @Mock
    private CacheLoader<DocumentLocaleKey, WordStatistic>
            documentStatisticLoader;

    @Produces @Mock
    private CacheLoader<DocumentLocaleKey, DocumentStatus> docStatusLoader;

    @Produces @Mock
    private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;

    @Produces @Mock
    private CacheLoader<VersionLocaleKey, WordStatistic> versionStatisticLoader;

    private UserTransaction tx;

    private final String projectSlug = "sample-project";

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/MergeTranslationsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void beforeMethod() throws Exception {
        tx = new TestTransaction(getEm());
        doReturn(tx).when(serviceLocator)
                .getJndiComponent("java:jboss/UserTransaction",
                        UserTransaction.class);
    }

    @Test
    @InRequestScope
    public void testMergeVersionNotExist() throws Exception {
        String sourceVersionSlug = "1.0";
        String targetVersionSlug = "non-exist-version";

        MergeTranslationsTaskHandle handle = new MergeTranslationsTaskHandle();
        Future<Void> future = service.startMergeTranslations(projectSlug,
                sourceVersionSlug, projectSlug, targetVersionSlug, true,
                handle);
        verifyZeroInteractions(identity);
        // wait for the async process to finish...
        future.get();
        // No translations were performed
        assertThat(handle.getTotalTranslations()).isEqualTo(0);
    }

    @Test
    @InRequestScope
    public void testMergeEmptyDoc() throws Exception {
        String sourceVersionSlug = "1.0";
        String targetVersionSlug = "3.0";

        MergeTranslationsTaskHandle handle = new MergeTranslationsTaskHandle();
        Future<Void> future = service.startMergeTranslations(projectSlug,
                sourceVersionSlug, projectSlug, targetVersionSlug, true, null);
        verifyZeroInteractions(identity);
        // wait for the async process to finish...
        future.get();
        // No translations were performed
        assertThat(handle.getTotalTranslations()).isEqualTo(0);
    }

    @Test
    @InRequestScope
    public void testMergeTranslations1() {
        String sourceVersionSlug = "1.0";
        String targetVersionSlug = "2.0";
        boolean useNewerTranslation = false;

        HProjectIteration expectedSourceVersion =
                projectIterationDAO.getBySlug(projectSlug, sourceVersionSlug);
        assertThat(expectedSourceVersion).isNotNull();

        HProjectIteration expectedTargetVersion =
                projectIterationDAO.getBySlug(projectSlug, targetVersionSlug);
        assertThat(expectedTargetVersion).isNotNull();

        List<HTextFlow[]> matchingTextFlows =
            textFlowDAO.getSourceByMatchedContext(
                expectedSourceVersion.getId(), expectedTargetVersion.getId(), 0,
                100);

        List<HLocale> locales =
                service.getSupportedLocales(projectSlug, targetVersionSlug);

        service.startMergeTranslations(projectSlug, sourceVersionSlug,
            projectSlug, targetVersionSlug, useNewerTranslation, null);

        List<HTextFlowTarget[]> expectedMergeData = Lists.newArrayList();
        for (HTextFlow[] data : matchingTextFlows) {
            for(HLocale locale: locales) {
                HTextFlowTarget sourceTft =
                        data[0].getTargets().get(locale.getId());
                HTextFlowTarget targetTft =
                        data[1].getTargets().get(locale.getId());

                if(targetTft == null) {
                    // If untranslated, create a dummy untranslated target
                    targetTft = new HTextFlowTarget(null, sourceTft.getLocale());
                }
                if (MergeTranslationsServiceImpl.shouldMerge(sourceTft,
                        targetTft, useNewerTranslation)) {
                    expectedMergeData.add(new HTextFlowTarget[] { sourceTft,
                            targetTft });
                }
            }
        }

        //entity in matchingTextFlows should be updated after merge process

        // check all results has same contents and states
        // check generated comments in [1]
        // check non translated/approved is not being used
        // check use latest translated if enabled
        for(HTextFlowTarget[] data: expectedMergeData) {
            assertThat(data[0].getState()).isIn(ContentState.TRANSLATED_STATES);
            assertThat(data[1].getState()).isEqualTo(data[0].getState());

            assertThat(data[0].getContents()).isEqualTo(data[1].getContents());
            assertThat(data[1].getRevisionComment()).contains(
                TranslationUtil.PREFIX_MERGE_VERSION);
            assertThat(data[1].getSourceType()).isEqualTo(
                TranslationSourceType.MERGE_VERSION);
        }
    }

    @Test
    public void testMergeTranslationWorkIsNotTranslated1() {
        Date now = new Date();
        HTextFlowTarget target1 = generateTarget(ContentState.NeedReview, now, "string1");
        HTextFlowTarget target2 = generateTarget(ContentState.Translated, now, "string2");

        testShouldMergeCondition(target1, target2, false, false);
    }

    @Test
    public void testMergeTranslationWorkIsNotTranslated2() {
        Date now = new Date();
        HTextFlowTarget target1 = generateTarget(ContentState.Translated, now, "string1");
        HTextFlowTarget target2 = generateTarget(ContentState.NeedReview, now, "string2");

        testShouldMergeCondition(target1, target2, false, true);
    }

    // target tft is has same modify date as source tft
    @Test
    public void testMergeTranslationWorkCheckDate1() {
        Date now = new Date();
        HTextFlowTarget target1 = generateTarget(ContentState.Translated, now, "string1");
        HTextFlowTarget target2 = generateTarget(ContentState.Translated, now, "string2");

        testShouldMergeCondition(target1, target2, false, false);
    }

    // target tft is newer than source tft
    @Test
    public void testMergeTranslationWorkCheckDate2() {
        Calendar c = new GregorianCalendar();
        HTextFlowTarget target1 =
                generateTarget(ContentState.Translated, c.getTime(), "string1");
        c.add(Calendar.DATE, 30);
        HTextFlowTarget target2 =
                generateTarget(ContentState.Translated, c.getTime(), "string2");

        testShouldMergeCondition(target1, target2, true, false);
    }

    // target tft is older than source tft
    @Test
    public void testMergeTranslationWorkCheckDate3() {
        Calendar c = new GregorianCalendar();
        HTextFlowTarget target2 =
                generateTarget(ContentState.Translated, c.getTime(), "string1");
        c.add(Calendar.DATE, 30);
        HTextFlowTarget target1 =
                generateTarget(ContentState.Translated, c.getTime(), "string2");

        testShouldMergeCondition(target1, target2, true, true);
    }

    // target tft is older than source tft, but same content
    @Test
    public void testMergeTranslationWorkSameStateAndContent() {
        String content = "content0";
        Calendar c = new GregorianCalendar();
        HTextFlowTarget target2 =
            generateTarget(ContentState.Translated, c.getTime(), content);
        c.add(Calendar.DATE, 30);
        HTextFlowTarget target1 =
            generateTarget(ContentState.Translated, c.getTime(), content);
        testShouldMergeCondition(target1, target2, false, false);
    }

    private HTextFlowTarget generateTarget(ContentState state,
            Date lastChanged, String content) {
        HTextFlowTarget target = new HTextFlowTarget();
        target.setState(state);
        target.setLastChanged(lastChanged);
        target.setContents(content);
        return target;
    }

    private void testShouldMergeCondition(HTextFlowTarget target1,
            HTextFlowTarget target2, boolean useNewerTranslation,
            boolean expectedResult) {
        boolean result =
                MergeTranslationsServiceImpl.shouldMerge(target1, target2,
                        useNewerTranslation);
        assertThat(result).isEqualTo(expectedResult);
    }
}
