package org.zanata.webtrans.server.rpc;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = { "jpa-tests" })
@Slf4j
public class GetTransUnitsNavigationHandlerTest extends ZanataDbunitJpaTest
{
   private GetTransUnitsNavigationHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private LocaleService localeService;
   @Mock
   private TextFlowSearchService textFlowSearchServiceImpl;
   private final DocumentId documentId = new DocumentId(1);
   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("performance/GetTransUnitListHandlerPerformanceTest.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      TextFlowDAO dao = new TextFlowDAO((Session) getEm().getDelegate());
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("localeServiceImpl", localeService)
            .use("textFlowDAO", dao)
            .use("textFlowSearchServiceImpl", textFlowSearchServiceImpl)
            .autowire(GetTransUnitsNavigationHandler.class);
      // @formatter:on
   }

   private void prepareActionAndMockLocaleService(GetTransUnitsNavigation action)
   {
      action.setEditorClientId(new EditorClientId("sessionId", 1));
      action.setWorkspaceId(TestFixture.workspaceId(new LocaleId("ja")));
      HLocale jaLocale = getEm().find(HLocale.class, 3L);
      ProjectIterationId projectIterationId = action.getWorkspaceId().getProjectIterationId();
      when(localeService.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), projectIterationId.getProjectSlug(), projectIterationId.getIterationSlug())).thenReturn(jaLocale);
   }

   @Test
   public void testExecuteWithNoFilterOptions() throws Exception
   {
      GetTransUnitsNavigation action = GetTransUnitsNavigation.newAction(new GetTransUnitActionContext(documentId));
      prepareActionAndMockLocaleService(action);

      GetTransUnitsNavigationResult result = handler.execute(action, null);

      assertThat(result.getTransIdStateList().size(), Matchers.equalTo(10));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(1L, ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(2L, ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(3L, ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(4L, ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(5L, ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(6L, ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(7L, ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(8L, ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(9L, ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(10L, ContentState.New));

      assertThat(result.getIdIndexList(), Matchers.contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
   }

   @Test
   public void testExecuteWithAllStatus() throws Exception
   {
      // filter with all status set to tru
      GetTransUnitActionContext context = new GetTransUnitActionContext(documentId).changeFilterNeedReview(true).changeFilterUntranslated(true).changeFilterTranslated(true);
      GetTransUnitsNavigation action = GetTransUnitsNavigation.newAction(context);
      prepareActionAndMockLocaleService(action);

      GetTransUnitsNavigationResult result = handler.execute(action, null);

      assertThat(result.getTransIdStateList().size(), Matchers.equalTo(10));
      assertThat(result.getIdIndexList(), Matchers.hasSize(10));
   }

   @Test
   public void testExecuteWithStatus() throws Exception
   {
      // filter fuzzy and new status
      GetTransUnitActionContext context = new GetTransUnitActionContext(documentId).changeFilterNeedReview(true).changeFilterUntranslated(true);
      GetTransUnitsNavigation action = GetTransUnitsNavigation.newAction(context);
      prepareActionAndMockLocaleService(action);

      GetTransUnitsNavigationResult result = handler.execute(action, null);

      assertThat(result.getTransIdStateList().size(), Matchers.equalTo(7));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(3L, ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(5L, ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(6L, ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(7L, ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(8L, ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(9L, ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(10L, ContentState.New));

      assertThat(result.getIdIndexList(), Matchers.contains(3L, 5L, 6L, 7L, 8L, 9L, 10L));
   }
}
