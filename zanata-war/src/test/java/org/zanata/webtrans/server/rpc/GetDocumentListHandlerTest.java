package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.file.FilePersistService;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationStateCache;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class GetDocumentListHandlerTest
{
   private GetDocumentListHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private DocumentDAO documentDAO;
   @Mock
   private TranslationFileService translationFileServiceImpl;
   @Mock
   private TranslationStateCache translationStateCacheImpl;
   @Mock
   private FilePersistService filePersistService;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("documentDAO", documentDAO)
            .use("translationFileServiceImpl", translationFileServiceImpl)
            .use("translationStateCacheImpl", translationStateCacheImpl)
            .use("filePersistService", filePersistService)
            .ignoreNonResolvable()
            .autowire(GetDocumentListHandler.class);
      // @formatter:on
   }

   @Test
   public void testExecute() throws Exception
   {
      WorkspaceId workspaceId = TestFixture.workspaceId();
      GetDocumentList action = new GetDocumentList();
      action.setWorkspaceId(workspaceId);
      HDocument hDocument = hDocument(1);
      List<HDocument> documentList = Arrays.asList(hDocument);

      when(documentDAO.getAllByProjectIteration("project", "master")).thenReturn(documentList);

      GetDocumentListResult result = handler.execute(action, null);

      verify(identity).checkLoggedIn();
      assertThat(result.getDocuments(), Matchers.hasSize(1));
      DocumentInfo documentInfo = result.getDocuments().get(0);
      assertThat(documentInfo.getId(), Matchers.equalTo(new DocumentId(new Long(1), "")));
      assertThat(documentInfo.getPath(), Matchers.equalTo("/dot/"));
      assertThat(documentInfo.getName(), Matchers.equalTo("a.po"));
   }

   @Test
   public void testExecuteWithFilter() throws Exception
   {
      WorkspaceId workspaceId = TestFixture.workspaceId();
      GetDocumentList action = new GetDocumentList();
      action.setWorkspaceId(workspaceId);
      HDocument hDocument = hDocument(1);
      List<HDocument> documentList = Arrays.asList(hDocument);
      when(documentDAO.getAllByProjectIteration("project", "master")).thenReturn(documentList);

      GetDocumentListResult result = handler.execute(action, null);

      assertThat(result.getDocuments(), Matchers.hasSize(1));
   }

   private HDocument hDocument(long id)
   {
      HProjectIteration iteration = new HProjectIteration();
      iteration.setProjectType(ProjectType.Podir);

      HDocument hDocument = new HDocument("/dot/a.po", ContentType.PO, new HLocale(LocaleId.EN_US));
      hDocument.setProjectIteration(iteration);
      TestFixture.setId(id, hDocument);
      return hDocument;
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
