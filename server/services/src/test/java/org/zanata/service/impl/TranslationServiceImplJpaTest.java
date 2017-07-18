package org.zanata.service.impl;

import java.util.List;
import java.util.Set;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.PerformanceProfiling;
import org.zanata.ZanataJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.i18n.Messages;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowBuilder;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import com.github.huangp.entityunit.entity.EntityMaker;
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LockManagerService;
import org.zanata.service.TranslationStateCache;
import org.zanata.test.CdiUnitRunner;
import org.zanata.transaction.TransactionUtilImpl;
import org.zanata.util.IServiceLocator;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
@AdditionalClasses({ LocaleServiceImpl.class, ValidationServiceImpl.class,
        TransactionUtilImpl.class })
public class TranslationServiceImplJpaTest extends ZanataJpaTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TranslationServiceImplJpaTest.class);

    @Inject
    TranslationServiceImpl service;
    private HAccount authenticatedAccount;
    private Set<String> extensions = Sets.newHashSet("gettext", "comment");
    @Produces
    @Mock
    ZanataIdentity identity;
    @Produces
    @Mock
    LockManagerService lockManagerService;
    @Produces
    @Mock
    TranslationStateCache translationStateCache;
    @Produces
    @Mock
    @FullText
    FullTextEntityManager fullTextEntityManager;
    @Produces
    @Mock
    Messages messages;
    @Produces
    @Mock
    IServiceLocator serviceLocator;
    @Mock
    UserTransaction userTransaction;

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

    @Produces
    @Authenticated
    HAccount getAuthenticatedAccount() {
        return authenticatedAccount;
    }

    @Before
    public void setUp() throws Exception {
        when(serviceLocator.getJndiComponent("java:jboss/UserTransaction",
                UserTransaction.class)).thenReturn(userTransaction);
        deleteAllTables();
        authenticatedAccount =
                EntityMakerBuilder.builder().includeOptionalOneToOne().build()
                        .makeAndPersist(getEm(), HAccount.class);
        HPerson person = EntityMakerBuilder.builder().build()
                .makeAndPersist(getEm(), HPerson.class);
        authenticatedAccount.setPerson(person);
    }

    @After
    public void cleanUp() {
        deleteAllTables();
    }
    // @Test(enabled = true, description = "this should only be executed
    // manually in IDE")

    @Ignore
    @Test
    @InRequestScope
    @PerformanceProfiling
    public void pushTranslation() {
        EntityMaker entityMaker = EntityMakerBuilder
                .builder().addFieldOrPropertyMaker(HProject.class,
                        "sourceViewURL", FixedValueMaker.EMPTY_STRING_MAKER)
                .build();
        HProjectIteration iteration =
                entityMaker.makeAndPersist(getEm(), HProjectIteration.class);
        HLocale srcLocale = createAndPersistLocale(LocaleId.EN_US, getEm());
        HLocale transLocale = createAndPersistLocale(LocaleId.DE, getEm());
        String versionSlug = iteration.getSlug();
        String projectSlug = iteration.getProject().getSlug();
        HDocument document =
                new HDocument("message", ContentType.PO, srcLocale);
        document.setProjectIteration(iteration);
        getEm().persist(document);
        getEm().flush();
        // adjust this number to suit testing purpose
        int numOfTextFlows = 50;
        int numOfTextFlowsHavingTarget = createSourceAndSomeTargets(document,
                transLocale, numOfTextFlows);
        getEm().getTransaction().commit();
        getEm().getTransaction().begin();
        Long targetsCountBefore = getEm()
                .createQuery(
                        "select count(*) from HTextFlowTarget where locale = :locale",
                        Long.class)
                .setParameter("locale", transLocale).getSingleResult();
        Assertions.assertThat(targetsCountBefore)
                .isEqualTo(numOfTextFlowsHavingTarget);
        // ============ add targets =========
        TranslationsResource translations = new TranslationsResource();
        translations.setRevision(1);
        for (int i = 0; i < numOfTextFlows; i++) {
            addSampleTranslation(translations, "res" + i);
        }
        Monitor mon = MonitorFactory.start("");
        log.info("==== start translateAllInDoc");
        service.translateAllInDoc(projectSlug, versionSlug, document.getDocId(),
                transLocale.getLocaleId(), translations, extensions,
                MergeType.AUTO, false, TranslationSourceType.API_UPLOAD);
        log.info("==== stop translateAllInDoc: {}", mon.stop());
        getEm().getTransaction().commit();
        getEm().getTransaction().begin();
        Long targetsCount = getEm()
                .createQuery(
                        "select count(*) from HTextFlowTarget where locale = :locale",
                        Long.class)
                .setParameter("locale", transLocale).getSingleResult();
        Assertions.assertThat(targetsCount).isEqualTo(numOfTextFlows);
        List<HTextFlowTargetHistory> histories =
                getEm().createQuery("from HTextFlowTargetHistory",
                        HTextFlowTargetHistory.class).getResultList();
        Assertions.assertThat(histories).hasSize(numOfTextFlowsHavingTarget);
    }

    private static HLocale createAndPersistLocale(LocaleId localeId,
            EntityManager entityManager) {
        HLocale hLocale = new HLocale(localeId, true, true);
        entityManager.persist(hLocale);
        return hLocale;
    }

    /**
     * Half of the HTextFlow will have target in it.
     *
     * @return number of textflows that has target in it.
     */
    private static int createSourceAndSomeTargets(HDocument doc,
            HLocale transLocale, int numOfTextFlows) {
        HTextFlowBuilder baseBuilder = new HTextFlowBuilder().withDocument(doc)
                .withTargetLocale(transLocale);
        int targetCount = 0;
        for (int i = 0; i < numOfTextFlows; i++) {
            HTextFlowBuilder builder = baseBuilder.withResId("res" + i)
                    .withSourceContent("hello world " + i);
            if (i % 2 == 0) {
                builder = builder.withTargetState(ContentState.NeedReview)
                        .withTargetContent("previous translation");
                targetCount++;
            }
            doc.getTextFlows().add(builder.build());
        }
        return targetCount;
    }

    private static void addSampleTranslation(TranslationsResource translations,
            String resId) {
        TextFlowTarget target = new TextFlowTarget(resId);
        target.setRevision(1);
        target.setState(ContentState.Translated);
        target.setContents("translated " + resId);
        translations.getTextFlowTargets().add(target);
    }
}
