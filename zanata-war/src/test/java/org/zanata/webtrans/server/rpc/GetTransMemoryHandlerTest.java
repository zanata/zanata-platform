package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.search.jpa.impl.FullTextEntityManagerImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationMemoryQueryService;
import org.zanata.service.ValidationService;
import org.zanata.service.impl.TranslationMemoryQueryServiceImpl;
import org.zanata.service.impl.TranslationStateCacheImpl;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "jpa-tests")
public class GetTransMemoryHandlerTest extends ZanataDbunitJpaTest {

    private static final int EXPECTED_MAX_RESULTS =
            GetTransMemoryHandler.MAX_RESULTS;

    private GetTransMemoryHandler handler;
    @Mock
    private ZanataIdentity identity;
    @Mock
    private LocaleService localeService;
    private LocaleId targetLocaleId = new LocaleId("ja");
    private LocaleId sourceLocaleId = LocaleId.EN_US;
    private TranslationMemoryQueryService translationMemoryQueryService;

    @Mock
    private ValidationService validationServiceImpl;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "performance/GetTransUnitListTest.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        SeamAutowire autoWireInstance = SeamAutowire.instance();
        autoWireInstance.use("identity", identity)
                .use("localeServiceImpl", localeService)
                .use("validationServiceImpl", validationServiceImpl)
                .use("entityManager", new FullTextEntityManagerImpl(getEm()))
                .use("session", getSession())
                .useImpl(TranslationStateCacheImpl.class);
        TranslationMemoryQueryServiceImpl queryService =
                autoWireInstance
                        .autowire(TranslationMemoryQueryServiceImpl.class);
        translationMemoryQueryService = spy(queryService);
        // @formatter:off
      autoWireInstance.use("translationMemoryQueryService", translationMemoryQueryService);
      handler = autoWireInstance.autowire(GetTransMemoryHandler.class);
      // @formatter:on
        when(localeService.getByLocaleId(targetLocaleId.getId())).thenReturn(
                getEm().find(HLocale.class, 3L));
    }

    @AfterMethod
    public void tearDown() {
        SeamAutowire.instance().reset();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecute() throws Exception {
        // Given: hibernate search finds 2 matches for query
        TransMemoryQuery query =
                new TransMemoryQuery(Lists.newArrayList("file removed"),
                        HasSearchType.SearchType.FUZZY_PLURAL);
        HTextFlowTarget tmMatch1 = getEm().find(HTextFlowTarget.class, 60L);
        HTextFlowTarget tmMatch2 = getEm().find(HTextFlowTarget.class, 62L);

        List<Object[]> targetMatches =
                Lists.newArrayList(new Object[] { 1.0F, tmMatch1 },
                        new Object[] { 1.1F, tmMatch2 });
        doReturn(targetMatches).when(translationMemoryQueryService)
                .getSearchResult(eq(query), eq(sourceLocaleId),
                        eq(targetLocaleId), eq(EXPECTED_MAX_RESULTS));

        GetTranslationMemory action =
                new GetTranslationMemory(query, targetLocaleId, sourceLocaleId);

        // When:
        GetTranslationMemoryResult result = handler.execute(action, null);

        // Then:
        verify(identity).checkLoggedIn();
        assertThat(result.getMemories(), Matchers.hasSize(2));
        assertThat(result.getMemories().get(0).getTargetContents(),
                Matchers.contains("adsf"));
        assertThat(result.getMemories().get(1).getTargetContents(),
                Matchers.contains("%d files removed"));
    }

    @Test
    public void searchReturnNotApprovedResult() throws Exception {
        // Given: hibernate search finds 2 matches for query and they are not
        // approved translation
        TransMemoryQuery query =
                new TransMemoryQuery(Lists.newArrayList("file removed"),
                        HasSearchType.SearchType.FUZZY_PLURAL);
        HTextFlow tmMatch1 =
                getEm().find(HTextFlowTarget.class, 61L).getTextFlow();
        List<Object[]> matches =
                Lists.newArrayList(new Object[] { 1.0F, tmMatch1 },
                        new Object[] { 1.1F, null });
        doReturn(matches).when(translationMemoryQueryService).getSearchResult(
                eq(query), eq(sourceLocaleId), eq(targetLocaleId),
                eq(EXPECTED_MAX_RESULTS));
        GetTranslationMemory action =
                new GetTranslationMemory(query, targetLocaleId, sourceLocaleId);

        // When:
        GetTranslationMemoryResult result = handler.execute(action, null);

        // Then:
        verify(identity).checkLoggedIn();
        assertThat(result.getMemories(), Matchers.hasSize(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenThereAreParseException() throws Exception {
        // Given: hibernate search can not parse query
        TransMemoryQuery query =
                new TransMemoryQuery("file removed",
                        HasSearchType.SearchType.RAW);
        doThrow(new ParseException("bad token")).when(
                translationMemoryQueryService).getSearchResult(eq(query),
                eq(sourceLocaleId), eq(targetLocaleId),
                eq(EXPECTED_MAX_RESULTS));
        GetTranslationMemory action =
                new GetTranslationMemory(query, targetLocaleId, sourceLocaleId);

        // When:
        GetTranslationMemoryResult result = handler.execute(action, null);

        // Then:
        verify(identity).checkLoggedIn();
        assertThat(result.getMemories(), Matchers.hasSize(0));
    }

    @Test
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
