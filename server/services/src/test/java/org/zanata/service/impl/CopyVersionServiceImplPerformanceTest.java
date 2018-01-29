/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.infinispan.manager.CacheContainer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.PerformanceProfiling;
import org.zanata.SlowTest;
import org.zanata.ZanataTest;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.file.FileSystemPersistService;
import org.zanata.i18n.Messages;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowBuilder;
import org.zanata.seam.security.CurrentUserImpl;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.EntityManagerFactoryRule;
import org.zanata.service.MockInitialContextRule;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.VersionLocaleKey;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.SessionId;
import org.zanata.test.CdiUnitRunner;
import org.zanata.transaction.TransactionUtil;
import org.zanata.transaction.TransactionUtilForUnitTest;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.DefaultLocale;
import org.zanata.util.Zanata;
import org.zanata.util.ZanataEntities;
import com.google.common.cache.CacheLoader;

import static com.github.huangp.entityunit.entity.EntityCleaner.deleteAll;

/**
 * This is a JUnit test that can setup large dataset and test the performance of
 * copy version.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ LocaleServiceImpl.class,
        TranslationMemoryServiceImpl.class,
        FileSystemPersistService.class,
        TranslationStateCache.class,
        VersionStateCacheImpl.class,
        ValidationServiceImpl.class,
        CurrentUserImpl.class
})
public class CopyVersionServiceImplPerformanceTest extends ZanataTest {
    private static final Logger log = LoggerFactory
            .getLogger(CopyVersionServiceImplPerformanceTest.class);


    private static final EntityManagerFactoryRule.TestProfile testProfile =
            EntityManagerFactoryRule.TestProfile.NormalBuild;

    @ClassRule
    public static EntityManagerFactoryRule emfRule =
            new EntityManagerFactoryRule(
                    testProfile);

    @ClassRule
    public static MockInitialContextRule initialContextRule =
            new MockInitialContextRule();

    protected EntityManager em;
    private int numOfTextFlows;
    private HProjectIteration copyVersionSource;

    @Inject
    private CopyVersionServiceImpl copyVersionService;

    // enable formatter off from Idea: settings -> Preferences > Editor > Code Style
    // @formatter:off
    @Produces @SessionId String sessionId = "";
    @Produces @ContextPath String contextPath = "";
    @Produces @Named("dswidQuery") String dswidQuery = "";
    @Produces @Named("dswidParam") String dswidParam = "";
    @Produces @DefaultLocale @Mock Messages messages;
    @Produces @DeltaSpike @Mock HttpSession httpSession;
    @Produces @Mock WindowContext windowContext;
    // @formatter:on
    private HAccount account;

    @Produces
    protected EntityManager getEm() {
        return em;
    }

    @Produces
    @FullText
    FullTextEntityManager getFullTextEntityManager() {
        return Search.getFullTextEntityManager(getEm());
    }

    @Produces
    @Zanata
    CacheContainer cacheContainer = new InfinispanTestCacheContainer();

    @Produces
    CacheLoader<VersionLocaleKey, WordStatistic> versionStatsCacheLoader =
            new CacheLoader<VersionLocaleKey, WordStatistic>() {
                @Override
                public WordStatistic load(VersionLocaleKey key)
                        throws Exception {
                    return new WordStatistic();
                }
            };

    @Produces
    TranslationStateCache translationStateCache =
            new TranslationStateCacheImpl();

    @Produces
    protected EntityManagerFactory getEmf() {
        return emfRule.getEmf();
    }

    @Produces
    protected Session getSession() {
        return (Session) em.getDelegate();
    }

    @Produces
    @Authenticated
    HAccount getAuthenticatedAccount() {
        return account;
    }

    @Produces
    protected TransactionUtil getTransactionUtil() {
        return new TransactionUtilForUnitTest(getEm());
    }


    @Before
    public void setUp() throws Exception {
        log.debug("Setting up EM");
        em = emfRule.getEmf().createEntityManager();
        em.getTransaction().begin();

        deleteAll(getEm(), ZanataEntities.entitiesForRemoval());

        account = new HAccount();
        account.setUsername("demo");
        getEm().persist(account);


        HLocale enUS = makeLocale(getEm(), LocaleId.EN_US);
        HLocale de = makeLocale(getEm(), LocaleId.DE);

        HProject project = makeProject(getEm(), "the-project");

        copyVersionSource = makeProjectVersion(getEm(), project, "copy-source");

        HDocument document =
                new HDocument("pot/message", ContentType.PO, enUS);
        document.setProjectIteration(copyVersionSource);

        HTextFlowBuilder textFlowBuilderForOldDoc =
                new HTextFlowBuilder().withDocument(document)
                        .withTargetLocale(enUS);

        // make many text flows all with translations in DE
        numOfTextFlows = 50;
        for (int i = 0; i < numOfTextFlows; i++) {
            textFlowBuilderForOldDoc.withResId("res" + i)
                    .withSourceContent("source " + i)
                    .withTargetContent("target " + i)
                    .withTargetLocale(de)
                    .withTargetState(ContentState.Translated).build();
        }

        getEm().persist(document);

        getEm().flush();
        getEm().clear();

//         Long totalTextFlows =
//         getEm().createQuery("select count(*) from HTextFlow",
//         Long.class).getSingleResult();
//
//         Assertions.assertThat(totalTextFlows).isEqualTo(numOfTextFlows);
//         Long totalTranslation =
//         getEm().createQuery("select count(*) from HTextFlowTarget",
//         Long.class).getSingleResult();
//         Assertions.assertThat(totalTranslation).isEqualTo(numOfTextFlows);

        // enable logging
        LogManager.getLogger(CopyTransServiceImpl.class.getPackage().getName())
                .setLevel(Level.DEBUG);
    }

    @After
    public void tearDown() {
        log.debug("Shutting down EM");
        // clear second level cache
        SessionFactory sessionFactory =
                ((Session) em.getDelegate()).getSessionFactory();
        try {
            sessionFactory.getCache().evictEntityRegions();
            sessionFactory.getCache().evictCollectionRegions();
        } catch (Exception e) {
            log.error(" *** Cache Exception " + e.getMessage());
        }
        em.getTransaction().rollback();
        if (em.isOpen()) {
            em.close();
        }
        em = null;
    }

    /**
     * We use {@link CopyVersionServiceImplPerformanceTest#testProfile }
     * to control which database (h2 or mysql) we will run the tests against. We
     * want to run h2 as part of the build to keep this test valid. We may want
     * to run mysql if we want to do performance profiling with larger data set
     * using larger {@link CopyVersionServiceImplPerformanceTest#numOfTextFlows}.
     *
     */
    //    @Ignore("slow test")
    @Test
    @InRequestScope
    @SlowTest
    @PerformanceProfiling
    public void testCopyVersion() throws Exception {
        copyVersionService.copyVersion(
                copyVersionSource.getProject().getSlug(),
                copyVersionSource.getSlug(), "copy-target", null);

        Long totalTranslation =
                getEm().createQuery("select count(*) from HTextFlowTarget",
                        Long.class).getSingleResult();
        Assertions.assertThat(totalTranslation).isEqualTo(numOfTextFlows * 2);
    }

    private static HLocale makeLocale(EntityManager em, LocaleId localeId) {
        HLocale hLocale = new HLocale(localeId);
        hLocale.setActive(true);
        hLocale.setEnabledByDefault(true);
        em.persist(hLocale);
        return hLocale;
    }

    private static HProject makeProject(EntityManager em, String slug) {
        HProject project = new HProject();
        project.setSlug(slug);
        project.setName(slug.toUpperCase());
        em.persist(project);
        return project;
    }

    private static HProjectIteration makeProjectVersion(EntityManager em,
            HProject project, String versionSlug) {
        HProjectIteration version = new HProjectIteration();
        version.setSlug(versionSlug);
        project.addIteration(version);
        em.persist(version);
        return version;
    }

}
