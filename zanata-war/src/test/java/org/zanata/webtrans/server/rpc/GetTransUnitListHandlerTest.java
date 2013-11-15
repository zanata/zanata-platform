package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.search.impl.FullTextSessionImpl;
import org.hibernate.search.jpa.impl.FullTextEntityManagerImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.TextFlowSearchServiceImpl;
import org.zanata.service.impl.TranslationStateCacheImpl;
import org.zanata.service.impl.ValidationServiceImpl;
import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = { "jpa-tests" })
@Slf4j
public class GetTransUnitListHandlerTest extends ZanataDbunitJpaTest {
    private GetTransUnitListHandler handler;
    @Mock
    private ZanataIdentity identity;
    @Mock
    private LocaleService localeService;
    @Mock
    private GetTransUnitsNavigationService getTransUnitsNavigationService;

    private final DocumentInfo document = TestFixture.documentInfo(1L, "");
    private final LocaleId localeId = new LocaleId("ja");
    private HLocale jaHLocale;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "performance/GetTransUnitListTest.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ResourceUtils resourceUtils = new ResourceUtils();
        resourceUtils.create(); // postConstruct
        TransUnitTransformer transUnitTransformer =
                SeamAutowire.instance().reset().use("resourceUtils", resourceUtils)
                        .autowire(TransUnitTransformer.class);

        SeamAutowire seam =
                SeamAutowire
                        .instance()
                        .use("localeServiceImpl", localeService)
                        .use("documentDAO", new DocumentDAO(getSession()))
                        .use("projectIterationDAO",
                                new ProjectIterationDAO(getSession()))
                        .use("entityManager",
                                new FullTextEntityManagerImpl(getEm()))
                        .use("session", new FullTextSessionImpl(getSession()))
                        .use("identity", identity)
                        .use("textFlowDAO", new TextFlowDAO(getSession()))
                        .use("transUnitTransformer", transUnitTransformer)
                        .use("webtrans.gwt.GetTransUnitsNavigationHandler",
                                getTransUnitsNavigationService)
                        .useImpl(TranslationStateCacheImpl.class)
                        .useImpl(TextFlowSearchServiceImpl.class)
                        .useImpl(ValidationServiceImpl.class).allowCycles();

        // @formatter:off
      handler = seam.autowire(GetTransUnitListHandler.class);
      // @formatter:on

        jaHLocale = getEm().find(HLocale.class, 3L);
    }

    private void prepareActionAndMockLocaleService(GetTransUnitList action) {
        action.setEditorClientId(new EditorClientId("sessionId", 1));
        action.setWorkspaceId(TestFixture.workspaceId(localeId, "plurals",
                "master", ProjectType.Podir));
        ProjectIterationId projectIterationId =
                action.getWorkspaceId().getProjectIterationId();
        when(
                localeService.validateLocaleByProjectIteration(action
                        .getWorkspaceId().getLocaleId(), projectIterationId
                        .getProjectSlug(), projectIterationId
                        .getIterationSlug())).thenReturn(jaHLocale);
        when(localeService.getByLocaleId(localeId)).thenReturn(jaHLocale);
    }

    @Test
    public void testExecuteToGetAll() throws Exception {
        GetTransUnitList action =
                GetTransUnitList.newAction(new GetTransUnitActionContext(
                        document));
        prepareActionAndMockLocaleService(action);

        long startTime = System.nanoTime();
        GetTransUnitListResult result = handler.execute(action, null);
        log.info("********** duration :{} second",
                (System.nanoTime() - startTime) / 1000000000.0);

        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(1, 2, 3, 4, 5));
    }

    @Test
    public void testExecuteWithStatusFilterOnly() throws Exception {
        GetTransUnitList action =
                GetTransUnitList.newAction(new GetTransUnitActionContext(
                        document).changeFilterFuzzy(true)
                        .changeFilterUntranslated(true));
        prepareActionAndMockLocaleService(action);

        GetTransUnitListResult result = handler.execute(action, null);

        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(3, 5, 6, 7, 8));
    }

    @Test
    public void testExecuteWithHasErrorFilterOnly() throws Exception {
        GetTransUnitList action =
                GetTransUnitList.newAction(new GetTransUnitActionContext(
                        document).changeFilterHasError(true));
        prepareActionAndMockLocaleService(action);

        GetTransUnitListResult result = handler.execute(action, null);

        log.info("result: {}", result);
        assertThat(result.getDocumentId(), Matchers.equalTo(document.getId()));
        assertThat(result.getGotoRow(), Matchers.equalTo(0));
        assertThat(TestFixture.asIds(result.getUnits()),
                Matchers.contains(1, 2, 3, 4, 5));
    }

    @Test
    public void testExecuteWithSearchOnly() throws Exception {
        // Given: we want to search for file (mixed case) and we change page
        // size
        // to 10 and start from index 2
        GetTransUnitList action =
                GetTransUnitList.newAction(new GetTransUnitActionContext(
                        document).changeFindMessage("FiLe").changeCount(10)
                        .changeOffset(1));
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
    public void testExecuteWithSearchAndStatusFilter() throws Exception {
        // Given: we want to search for file (mixed case) in fuzzy and
        // untranslated text flows
        GetTransUnitList action =
                GetTransUnitList
                        .newAction(new GetTransUnitActionContext(document)
                                .changeFindMessage("FiLe")
                                .changeFilterUntranslated(true)
                                .changeFilterFuzzy(true));
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
    public void testExecuteWithSearchAndStatusFilter2() throws Exception {
        GetTransUnitList action =
                GetTransUnitList.newAction(new GetTransUnitActionContext(
                        document).changeFindMessage("FiLe")
                        .changeFilterUntranslated(true).changeFilterFuzzy(true)
                        .changeFilterHasError(true));
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
    public void testExecuteWithPageSize() throws Exception {
        /**
         * Client request for page 4 data
         */
        int offset = 76;
        int countPerPage = 25;

        GetTransUnitList action =
                GetTransUnitList.newAction(new GetTransUnitActionContext(
                        document).changeFindMessage("FiLe")
                        .changeFilterUntranslated(true).changeFilterFuzzy(true)
                        .changeFilterHasError(true).changeOffset(offset)
                        .changeCount(countPerPage));

        prepareActionAndMockLocaleService(action);

        // When:
        GetTransUnitListResult result = handler.execute(action, null);

        assertThat(result.getTargetPageIndex(), Matchers.equalTo(3));
    }

    @Test
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

        GetTransUnitList action =
                GetTransUnitList.newAction(new GetTransUnitActionContext(
                        document).changeFindMessage("FiLe")
                        .changeFilterUntranslated(true).changeFilterFuzzy(true)
                        .changeFilterHasError(true).changeOffset(offset)
                        .changeCount(countPerPage));
        action.setNeedReloadIndex(true);

        prepareActionAndMockLocaleService(action);

        // When:
        when(
                getTransUnitsNavigationService.getNavigationIndexes(
                        isA(GetTransUnitsNavigation.class), isA(HLocale.class)))
                .thenReturn(navigationResult);
        when(navigationResult.getIdIndexList()).thenReturn(idIndexList);

        GetTransUnitListResult result = handler.execute(action, null);

        assertThat(result.getTargetPageIndex(), Matchers.equalTo(2));
    }
}
