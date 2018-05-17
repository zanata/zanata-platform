package org.zanata.service.tm.merge;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
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
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TransMemoryUnitDAO;
import org.zanata.email.HtmlEmailSender;
import org.zanata.email.HtmlEmailStrategy;
import org.zanata.email.TMMergeEmailContext;
import org.zanata.email.TMMergeEmailStrategy;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.events.TransMemoryMergeEvent;
import org.zanata.events.TransMemoryMergeProgressEvent;
import org.zanata.i18n.Messages;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemoryUnitVariant;
import org.zanata.rest.dto.VersionTMMerge;
import org.zanata.seam.security.CurrentUserImpl;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LockManagerService;
import org.zanata.service.TextFlowCounter;
import org.zanata.service.ValidationService;
import org.zanata.service.VersionStateCache;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.service.impl.TranslationMemoryServiceImpl;
import org.zanata.service.impl.TranslationServiceImpl;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.test.CdiUnitRunner;
import org.zanata.transaction.TransactionUtil;
import org.zanata.transaction.TransactionUtilForUnitTest;
import org.zanata.util.UrlUtil;
import org.zanata.util.Zanata;
import org.zanata.webtrans.shared.rest.dto.InternalTMSource;
import org.zanata.webtrans.shared.rpc.MergeRule;
import kotlin.ranges.IntRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.email.Addresses.getAddress;
import static org.zanata.service.TransMemoryMergeService.BATCH_SIZE;
import static org.zanata.test.EntityTestData.makeTextFlowTarget;

@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ ProjectIterationDAO.class, TextFlowDAO.class,
        TransMemoryUnitDAO.class, LocaleServiceImpl.class,
        TranslationMemoryServiceImpl.class, CurrentUserImpl.class,
        TranslationServiceImpl.class })
public class TransMemoryMergeServiceImplJpaTest extends ZanataJpaTest {

    // TODO test with smaller batches. 22 TextFlows with batchSize=5 would run more quickly, and tell us more.
    // One and a bit batches:
    private static final int numOfTextFlows = BATCH_SIZE + 2;

    private MergeTranslationsTaskHandle mergeHandle = new MergeTranslationsTaskHandle(
            new GenericAsyncTaskKey("textKeyId"));

    @Inject
    private TransMemoryMergeServiceImpl service;

    @Produces
    @Mock
    private UrlUtil urlUtil;
    @Produces
    @Mock
    private ZanataIdentity identity;
    @Produces
    @ServerPath
    private String serverPath = "http://example.com/";
    @Produces
    private TMBandDefs bands = new TMBandDefsProducer().produce("80 90");
    @Produces @Mock
    private Messages messages;
    @Produces @Mock
    private HtmlEmailSender emailSender;
    private HLocale targetLocale;
    private HLocale sourceLocale;
    private TransMemory tmx;
    private HProjectIteration sourceVersion;
    private HProjectIteration targetVersion;
    private HDocument sourceDoc;
    private HDocument targetDoc;
    @Captor
    private ArgumentCaptor<HtmlEmailStrategy> emailStrategy;
    private HPerson person;

    @Produces
    @Authenticated
    private
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
    @Produces
    @Mock
    private LockManagerService lockManagerService;
    @Produces
    @Mock
    private ValidationService validationService;

    @Produces
    TransactionUtil transactionUtil() {
        return new TransactionUtilForUnitTest(getEmf().createEntityManager(), true);
    }

    @Produces
    @FullText
    @Zanata
    @Default
    FullTextEntityManager fullTextEntityManager() {
        return Search.getFullTextEntityManager(getEm());
    }

    @Produces
    Session session() {
        return getSession();
    }

    @Before
    public void setUp() {
        purgeLuceneIndexes();
        MockitoAnnotations.initMocks(this);
        person = new HPerson();
        person.setEmail("person@example.com");
        person.setName("Person Name");
        authenticated.setPerson(person);
        sourceLocale = new HLocale(LocaleId.EN);
        targetLocale = new HLocale(LocaleId.DE);
        targetLocale.setEnabledByDefault(true);
        targetLocale.setActive(true);
        tmx = new TransMemory();
        tmx.setSlug("tmx");
        HProject project = new HProject();
        project.setSlug("project");
        project.setName("project");

        sourceVersion = createVersion(project, "release");
        targetVersion = createVersion(project, "master");

        sourceDoc = new HDocument("doc", ContentType.PO, sourceLocale);
        sourceDoc.setProjectIteration(sourceVersion);

        targetDoc = new HDocument("doc", ContentType.PO, sourceLocale);
        targetDoc.setProjectIteration(targetVersion);

        doInTransaction(em -> {
            em.persist(person);
            em.persist(sourceLocale);
            em.persist(targetLocale);
            em.persist(tmx);
            em.persist(project);
            em.persist(sourceVersion);
            em.persist(targetVersion);
            em.persist(sourceDoc);
            em.persist(targetDoc);
            em.flush();
        });
    }

    private HProjectIteration createVersion(HProject project, String versionSlug) {
        HProjectIteration version = new HProjectIteration();
        version.setSlug(versionSlug);
        version.setProject(project);
        project.addIteration(version);
        return version;
    }

    @After
    public void tearDown() {
        deleteAllTables();
        purgeLuceneIndexes();
    }

    @Test
    @InRequestScope
    public void mergeVersionFromImportedTMOnly() throws Exception {
        // given

        doInTransaction(em -> {
            // template content for both HTextFlow and TransMemoryUnit
            String content = "some text to match";
            IntStream.rangeClosed(1, numOfTextFlows).forEach(i -> {
                em.persist(makeTransMemoryUnit(tmx, i, content));
                makeTextFlow(targetDoc, i, content);
            });
            em.merge(targetDoc);
        });

        int threshold = 100;

        // when

        service.startMergeTranslations(
                targetVersion.getId(),
                new VersionTMMerge(targetLocale.getLocaleId(), threshold,
                        MergeRule.FUZZY, MergeRule.REJECT, MergeRule.REJECT,
                        MergeRule.FUZZY, InternalTMSource.SELECT_NONE),
                mergeHandle).get();

        // then

        checkMergeResults(threshold, ContentState.NeedReview);
    }

    @Test
    @InRequestScope
    public void mergeVersionFromAnotherVersion() throws Exception {
        // given

        doInTransaction(em -> {
            sourceDoc = em.find(HDocument.class, sourceDoc.getId());
            targetDoc = em.find(HDocument.class, targetDoc.getId());
            // template content for HTextFlow in source and target versions
            String content = "DIFFERENT TEXT TO FIND";
            IntStream.rangeClosed(1, numOfTextFlows).forEach(i -> {
                makeTextFlow(sourceDoc, i, content);
                em.merge(sourceDoc);
                makeTextFlow(targetDoc, i, content);
                em.merge(targetDoc);
            });

            // template content for HTextFlow in source and target versions
            IntStream.rangeClosed(1, numOfTextFlows).forEach(i -> {
                HTextFlow sourceTextFlow = sourceDoc.getAllTextFlows().get("resId" + i);
                HTextFlowTarget sourceTFTarget =
                        makeTextFlowTarget(sourceTextFlow, targetLocale,
                                ContentState.Approved);
                sourceTFTarget.setContent0("translation of " + sourceTextFlow.getContents().get(0));
                sourceTextFlow.getTargets().put(targetLocale.getId(), sourceTFTarget);
                em.merge(sourceTextFlow);
            });
        });

        // make the translations in TM visible to the user
        when(identity.hasPermission(any(), any())).thenReturn(true);

        int threshold = 80;

        // when

        service.startMergeTranslations(
                targetVersion.getId(),
                new VersionTMMerge(targetLocale.getLocaleId(), threshold,
                        MergeRule.FUZZY, MergeRule.REJECT, MergeRule.REJECT,
                        MergeRule.FUZZY, InternalTMSource.SELECT_ALL),
                mergeHandle).get();

        // then


        checkMergeResults(threshold, ContentState.Translated);
    }

    private void checkMergeResults(
            int threshold,
            ContentState expectedCopyState) {
        assertThat(mergeHandle.getTotalTextFlows()).isEqualTo(numOfTextFlows);
        assertThat(mergeHandle.getMaxProgress()).isEqualTo(numOfTextFlows);
        assertThat(mergeHandle.getCurrentProgress()).isEqualTo(numOfTextFlows);

        verify(emailSender).sendMessage(emailStrategy.capture());

        assertThat(emailStrategy.getValue()).isInstanceOf(TMMergeEmailStrategy.class);
        TMMergeEmailStrategy str =
                (TMMergeEmailStrategy) emailStrategy.getValue();
        TMMergeEmailContext mergeContext = str.getContext();
        assertThat(mergeContext.getMatchRange()).isEqualTo(new IntRange(threshold, 100));
        assertThat(mergeContext.getProject().getName()).isEqualTo(targetVersion.getProject().getName());
        assertThat(mergeContext.getVersion().getSlug()).isEqualTo(targetVersion.getSlug());
        assertThat(mergeContext.getToAddresses()).containsExactly(
                getAddress(person));

        TMMergeResult mergeResult = str.getMergeResult();
        TextFlowCounter textFlowCounter = mergeResult.getCounter(
                expectedCopyState,
                        new IntRange(100, 100));

        assertThat(textFlowCounter.getMessages()).isEqualTo(numOfTextFlows);
    }

    private void makeTextFlow(HDocument doc, int index, String content) {
        String sourceContent = content + index;
        HTextFlow hTextFlow =
                new HTextFlow(doc, "resId" + index, sourceContent);
        doc.getTextFlows().add(hTextFlow);
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
