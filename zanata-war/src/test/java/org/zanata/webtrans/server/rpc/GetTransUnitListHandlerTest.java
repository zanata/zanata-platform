package org.zanata.webtrans.server.rpc;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
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
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;

import ch.qos.logback.classic.Level;
import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = { "jpa-tests" })
@Slf4j
public class GetTransUnitListHandlerTest extends ZanataDbunitJpaTest
{
   private GetTransUnitListHandler handler;
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
      beforeTestOperations.add(new DataSetOperation("performance/GetTransUnitListTest.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      TextFlowDAO dao = new TextFlowDAO((Session) getEm().getDelegate());
      ResourceUtils resourceUtils = new ResourceUtils();
      resourceUtils.create(); //postConstruct
      TransUnitTransformer transUnitTransformer = SeamAutowire.instance().use("resourceUtils", resourceUtils).autowire(TransUnitTransformer.class);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("localeServiceImpl", localeService)
            .use("textFlowDAO", dao)
            .use("transUnitTransformer", transUnitTransformer)
            .use("textFlowSearchServiceImpl", textFlowSearchServiceImpl)
            .autowire(GetTransUnitListHandler.class);
      // @formatter:on
   }

   private void prepareActionAndMockLocaleService(GetTransUnitList action)
   {
      action.setEditorClientId(new EditorClientId("sessionId", 1));
      action.setWorkspaceId(TestFixture.workspaceId(new LocaleId("ja")));
      HLocale jaLocale = getEm().find(HLocale.class, 3L);
      ProjectIterationId projectIterationId = action.getWorkspaceId().getProjectIterationId();
      when(localeService.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), projectIterationId.getProjectSlug(), projectIterationId.getIterationSlug())).thenReturn(jaLocale);
   }

   @Test
   public void testExecuteToGetAll() throws Exception
   {
      GetTransUnitList action = GetTransUnitList.newAction(new GetTransUnitActionContext(documentId));
      prepareActionAndMockLocaleService(action);

      long startTime = System.nanoTime();
      GetTransUnitListResult result = handler.execute(action, null);
      log.info("********** duration :{} second", (System.nanoTime() - startTime) / 1000000000.0);

      log.info("result: {}", result);
      assertThat(result.getDocumentId(), Matchers.equalTo(documentId));
      assertThat(result.getGotoRow(), Matchers.equalTo(0));
      assertThat(TestFixture.asIds(result.getUnits()), Matchers.contains(1, 2, 3, 4, 5));
   }


   @Test
   public void testExecuteToGetByStatus() throws Exception
   {
      GetTransUnitList action = GetTransUnitList.newAction(new GetTransUnitActionContext(documentId).changeFilterNeedReview(true));
      prepareActionAndMockLocaleService(action);

      GetTransUnitListResult result = handler.execute(action, null);

      log.info("result: {}", result);
      assertThat(result.getDocumentId(), Matchers.equalTo(documentId));
      assertThat(result.getGotoRow(), Matchers.equalTo(0));
      assertThat(TestFixture.asIds(result.getUnits()), Matchers.contains(3, 5, 6));
   }

   @Test
   public void selectDocument()
   {
      HDocument hDocument = getEm().find(HDocument.class, 1L);
   }
}
