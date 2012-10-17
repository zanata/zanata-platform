package org.zanata.webtrans.server.rpc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
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
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;

import lombok.extern.slf4j.Slf4j;
import static org.mockito.Mockito.*;
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
      TransUnitTransformer transUnitTransformer = SeamAutowire.instance().use("resourceUtils", new ResourceUtils()).autowire(TransUnitTransformer.class);
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

   @Test
   public void testExecute() throws Exception
   {
      GetTransUnitList action = GetTransUnitList.newAction(new GetTransUnitActionContext(new DocumentId(1)));
      action.setEditorClientId(new EditorClientId("sessionId", 1));
      action.setWorkspaceId(TestFixture.workspaceId(new LocaleId("ja")));
      HLocale jaLocale = getEm().find(HLocale.class, 3L);
      ProjectIterationId projectIterationId = action.getWorkspaceId().getProjectIterationId();
      when(localeService.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), projectIterationId.getProjectSlug(), projectIterationId.getIterationSlug())).thenReturn(jaLocale);

      GetTransUnitListResult result = handler.execute(action, null);

      log.info("result: {}", result);
   }
}
