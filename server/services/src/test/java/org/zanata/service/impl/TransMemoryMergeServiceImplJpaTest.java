package org.zanata.service.impl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.Future;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jetbrains.annotations.NotNull;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataJpaTest;
import org.zanata.async.GenericAsyncTaskKey;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TransMemoryUnitDAO;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.events.TransMemoryMergeEvent;
import org.zanata.events.TransMemoryMergeProgressEvent;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemoryUnitVariant;
import org.zanata.rest.dto.VersionTMMerge;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.service.TranslationService;
import org.zanata.service.VersionStateCache;
import org.zanata.test.CdiUnitRunner;
import org.zanata.transaction.TransactionUtil;
import org.zanata.transaction.TransactionUtilForUnitTest;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rest.dto.InternalTMSource;
import org.zanata.webtrans.shared.rpc.MergeRule;

import com.google.common.collect.Lists;

@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ ProjectIterationDAO.class, TextFlowDAO.class,
        TransMemoryUnitDAO.class, LocaleServiceImpl.class,
        TranslationMemoryServiceImpl.class })
public class TransMemoryMergeServiceImplJpaTest extends ZanataJpaTest {

    @Inject
    private TransMemoryMergeServiceImpl service;

    @Produces
    @Mock
    private UrlUtil urlUtil;
    @Produces
    @Mock
    private ZanataIdentity identity;
    private HLocale targetLocale;
    private HLocale sourceLocale;
    private TransMemory tmx;
    private HProjectIteration targetVersion;
    private HDocument doc;
    @Captor
    private ArgumentCaptor<List<TransUnitUpdateRequest>> translationRequestCaptor;

    @Produces
    TransactionUtil transactionUtil() {
        return new TransactionUtilForUnitTest(getEmf().createEntityManager(), true);
    }

    @Produces
    @FullText
    FullTextEntityManager fullTextEntityManager() {
        return Search.getFullTextEntityManager(getEm());
    }

    @Produces
    Session session() {
        return getSession();
    }

    @Produces
    @Mock
    private TranslationService translationService;
    @Produces
    @Authenticated
    HAccount authenticated = new HAccount();

    @Produces
    @Mock
    private Event<TextFlowTargetUpdateContextEvent> textFlowTargetUpdateContextEvent;

    @Produces
    @Mock
    private Event<TransMemoryMergeEvent> transMemoryMergeEvent;
    @Produces
    @Mock
    private Event<TransMemoryMergeProgressEvent> transMemoryMergeProgressEvent;
    @Produces
    @Mock
    private VersionStateCache versionStateCache;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sourceLocale = new HLocale(LocaleId.EN);
        targetLocale = new HLocale(LocaleId.DE);
        targetLocale.setEnabledByDefault(true);
        targetLocale.setActive(true);
        tmx = new TransMemory();
        tmx.setSlug("tmx");
        HProject project = new HProject();
        project.setSlug("project");
        project.setName("project");

        targetVersion = new HProjectIteration();
        targetVersion.setSlug("master");
        project.addIteration(targetVersion);

        doc = new HDocument("doc", ContentType.PO, sourceLocale);
        doc.setProjectIteration(targetVersion);
        doInTransaction(em -> {
            em.persist(sourceLocale);
            em.persist(targetLocale);
            em.persist(project);
            em.persist(targetVersion);
            em.persist(doc);
            em.persist(tmx);
        });
    }

    @After
    public void tearDown() {
        deleteAllTables();
    }

    @Test
    @InRequestScope
    public void startMergeTranslationsFromImportedTMOnly() throws Exception {
        int numOfTextFlows = 100;
        doInTransaction(em -> {
            // template content for both HTextFlow and TransMemoryUnit
            String content = "some text to match";
            javaslang.collection.List.rangeClosed(1, numOfTextFlows)
                    .forEach(i -> {
                        em.persist(makeTransMemoryUnit(tmx, i, content));
                        makeTextFlow(doc, i, content);
                    });
            em.merge(doc);
        });

        Future<Void> future = service.startMergeTranslations(
                targetVersion.getId(),
                new VersionTMMerge(targetLocale.getLocaleId(), 100,
                        MergeRule.FUZZY, MergeRule.REJECT, MergeRule.REJECT, MergeRule.FUZZY,
                        InternalTMSource.SELECT_NONE),
                new MergeTranslationsTaskHandle(
                        new GenericAsyncTaskKey("textKeyId")));

        future.get();

        int expectedBatchNumber =
                calculateExpectedNumberOfBatchRun(numOfTextFlows);
        verify(translationService, times(expectedBatchNumber)).translate(
                eq(targetLocale.getLocaleId()),
                translationRequestCaptor.capture());

        List<TransUnitUpdateRequest> allRequests = Lists.newLinkedList();
        translationRequestCaptor.getAllValues().forEach(allRequests::addAll);
        // we should all find matches
        Assertions.assertThat(allRequests).hasSize(numOfTextFlows);
    }

    public static int calculateExpectedNumberOfBatchRun(double numOfTextFlows) {
        return (int) Math.ceil(
                numOfTextFlows / TransMemoryMergeService.BATCH_SIZE);
    }

    @NotNull
    private HTextFlow makeTextFlow(HDocument doc, int index, String content) {
        String sourceContent = content + index;
        HTextFlow hTextFlow =
                new HTextFlow(doc, "resId" + index, sourceContent);
        doc.getTextFlows().add(hTextFlow);
        return hTextFlow;
    }

    private static TransMemoryUnit makeTransMemoryUnit(TransMemory tmx,
            int index, String content) {
        String id = String.format("doc:resId%d", index);
        String sourceContent = content + index;
        String translationContent = "translation of " + sourceContent;
        return TransMemoryUnit.tu(tmx, id, id, LocaleId.EN.getId(),
                wrapInTag(sourceContent), TransMemoryUnitVariant.tuv(
                        LocaleId.DE.getId(), wrapInTag(translationContent)));
    }

    private static String wrapInTag(String content) {
        return String.format("<seg>%s</seg>", content);
    }

}
