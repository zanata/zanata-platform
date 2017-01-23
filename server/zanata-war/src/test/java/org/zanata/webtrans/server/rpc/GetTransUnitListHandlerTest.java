package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.cache.CacheLoader;
import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.infinispan.manager.CacheContainer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.jpa.FullText;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.TextFlowSearchServiceImpl;
import org.zanata.service.impl.TranslationStateCacheImpl;
import org.zanata.service.impl.ValidationServiceImpl;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.Zanata;
import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ TranslationStateCacheImpl.class,
        TextFlowSearchServiceImpl.class, ValidationServiceImpl.class })
public class GetTransUnitListHandlerTest extends ZanataDbunitJpaTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(GetTransUnitListHandlerTest.class);

    @Inject
    @Any
    private GetTransUnitListHandler handler;
    @Produces
    @Mock
    private ZanataIdentity identity;
    @Produces
    @Mock
    private LocaleService localeService;
    @Produces
    @Mock
    private GetTransUnitsNavigationService getTransUnitsNavigationService;
    @Produces
    @Mock
    private ResourceUtils resourceUtils;
    @Produces
    @Zanata
    private CacheContainer cacheContainer = new InfinispanTestCacheContainer();
    @Produces
    @FullText
    @Mock
    FullTextSession fullTextSession;
    @Produces
    @FullText
    @Mock
    FullTextEntityManager fullTextEntityManager;
    @Produces
    @Mock
    private CacheLoader<DocumentLocaleKey, WordStatistic> documentStatisticLoader;
    @Produces
    @Mock
    private CacheLoader<DocumentLocaleKey, DocumentStatus> docStatusLoader;
    @Produces
    @Mock
    private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;

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

    private final DocumentInfo document = TestFixture.documentInfo(1L, "");
    private final LocaleId localeId = new LocaleId("ja");
    private HLocale jaHLocale;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "performance/GetTransUnitListTest.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void setUp() throws Exception {
        jaHLocale = getEm().find(HLocale.class, 3L);
    }

    private void prepareActionAndMockLocaleService(GetTransUnitList action) {
        action.setEditorClientId(new EditorClientId("sessionId", 1));
        action.setWorkspaceId(TestFixture.workspaceId(localeId, "plurals",
                "master", ProjectType.Podir));
        ProjectIterationId projectIterationId =
                action.getWorkspaceId().getProjectIterationId();
        when(localeService.validateLocaleByProjectIteration(
                action.getWorkspaceId().getLocaleId(),
                projectIterationId.getProjectSlug(),
                projectIterationId.getIterationSlug())).thenReturn(jaHLocale);
        when(localeService.getByLocaleId(localeId)).thenReturn(jaHLocale);
    }

    @Test
    @InRequestScope
    public void testExecuteToGetAll() throws Exception {
        GetTransUnitList action = GetTransUnitList
                .newAction(new GetTransUnitActionContext(document));
        prepareActionAndMockLocaleService(action);
        long startTime = System.nanoTime();
        GetTransUnitListResult result = handler.execute(action, null);
        log.info("********** duration :{} second",
                (System.nanoTime() - startTime) / 1.0E9);
        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(1, 2, 3, 4, 5));
    }

    @Test
    @InRequestScope
    public void testExecuteWithStatusFilterOnly() throws Exception {
        GetTransUnitList action = GetTransUnitList
                .newAction(new GetTransUnitActionContext(document)
                        .withFilterFuzzy(true).withFilterUntranslated(true));
        prepareActionAndMockLocaleService(action);
        GetTransUnitListResult result = handler.execute(action, null);
        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(3, 5, 6, 7, 8));
    }

    @Test
    @InRequestScope
    public void testExecuteWithHasErrorFilterOnly() throws Exception {
        GetTransUnitList action = GetTransUnitList
                .newAction(new GetTransUnitActionContext(document)
                        .withFilterHasError(true));
        prepareActionAndMockLocaleService(action);
        GetTransUnitListResult result = handler.execute(action, null);
        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(1, 2, 3, 4, 5));
    }

    @Test
    @InRequestScope
    public void testExecuteWithSearchOnly() throws Exception {
        // Given: we want to search for file (mixed case) and we change page
        // size
        // to 10 and start from index 2
        GetTransUnitList action = GetTransUnitList
                .newAction(new GetTransUnitActionContext(document)
                        .withFindMessage("FiLe").withCount(10).withOffset(1));
        prepareActionAndMockLocaleService(action);
        // When:
        GetTransUnitListResult result = handler.execute(action, null);
        // Then:
        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(2, 3, 4, 5, 6, 8));
    }

    @Test
    @InRequestScope
    public void testExecuteWithSearchAndStatusFilter() throws Exception {
        // Given: we want to search for file (mixed case) in fuzzy and
        // untranslated text flows
        GetTransUnitList action = GetTransUnitList.newAction(
                new GetTransUnitActionContext(document).withFindMessage("FiLe")
                        .withFilterUntranslated(true).withFilterFuzzy(true));
        prepareActionAndMockLocaleService(action);
        // When:
        GetTransUnitListResult result = handler.execute(action, null);
        // Then:
        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(3, 5, 6, 8));
    }

    @Test
    @InRequestScope
    public void testExecuteWithSearchAndStatusFilter2() throws Exception {
        GetTransUnitList action = GetTransUnitList
                .newAction(new GetTransUnitActionContext(document)
                        .withFindMessage("FiLe").withFilterUntranslated(true)
                        .withFilterFuzzy(true).withFilterHasError(true));
        prepareActionAndMockLocaleService(action);
        // When:
        GetTransUnitListResult result = handler.execute(action, null);
        // Then:
        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(3, 5, 6, 8));
    }

    @Test
    @InRequestScope
    public void testExecuteWithPageSize() throws Exception {
        int offset = 76;
        int countPerPage = 25;
        GetTransUnitList action = GetTransUnitList
                .newAction(new GetTransUnitActionContext(document)
                        .withFindMessage("FiLe").withFilterUntranslated(true)
                        .withFilterFuzzy(true).withFilterHasError(true)
                        .withOffset(offset).withCount(countPerPage));
        prepareActionAndMockLocaleService(action);
        // When:
        GetTransUnitListResult result = handler.execute(action, null);
        assertThat(result.getTargetPageIndex(), Matchers.equalTo(3));
    }

    /**
     * Client request for page 4 data
     */
    @Test
    @InRequestScope
    public void testExecuteWithPageSizeNeedReload() throws Exception {
        /**
         * Client request for page 4 data - Offset:75 Count per page: 25
         * Assuming tft from getTransUnitsNavigationService.getNavigationIndexes
         * = 74
         */
        int offset = 75;
        int countPerPage = 25;
        GetTransUnitsNavigationResult navigationResult =
                mock(GetTransUnitsNavigationResult.class);
        List<TransUnitId> idIndexList = new ArrayList<TransUnitId>();
        for (int i = 1; i < offset; i++) {
            idIndexList.add(new TransUnitId(i));
        }
        GetTransUnitList action = GetTransUnitList
                .newAction(new GetTransUnitActionContext(document)
                        .withFindMessage("FiLe").withFilterUntranslated(true)
                        .withFilterFuzzy(true).withFilterHasError(true)
                        .withOffset(offset).withCount(countPerPage));
        action.setNeedReloadIndex(true);
        prepareActionAndMockLocaleService(action);
        // When:
        when(getTransUnitsNavigationService.getNavigationIndexes(
                isA(GetTransUnitsNavigation.class), isA(HLocale.class)))
                        .thenReturn(navigationResult);
        when(navigationResult.getIdIndexList()).thenReturn(idIndexList);
        GetTransUnitListResult result = handler.execute(action, null);
        assertThat(result.getTargetPageIndex(), Matchers.equalTo(2));
    }
}
