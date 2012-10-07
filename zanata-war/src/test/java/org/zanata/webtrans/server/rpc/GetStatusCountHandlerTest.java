package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetStatusCount;
import org.zanata.webtrans.shared.rpc.GetStatusCountResult;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class GetStatusCountHandlerTest
{
   private GetStatusCountHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private DocumentDAO documentDAO;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("documentDAO", documentDAO)
            .ignoreNonResolvable()
            .autowire(GetStatusCountHandler.class);
      // @formatter:on
   }

   @Test
   public void testExecute() throws Exception
   {
      DocumentId documentId = new DocumentId(1);
      WorkspaceId workspaceId = TestFixture.workspaceId();
      GetStatusCount action = new GetStatusCount(documentId);
      action.setWorkspaceId(workspaceId);
      TranslationStats stats = new TranslationStats();
      when(documentDAO.getStatistics(documentId.getId(), workspaceId.getLocaleId())).thenReturn(stats);

      GetStatusCountResult result = handler.execute(action, null);

      verify(identity).checkLoggedIn();
      verify(documentDAO).getStatistics(documentId.getId(), workspaceId.getLocaleId());
      assertThat(result.getCount(), Matchers.sameInstance(stats));
      assertThat(result.getDocumentId(), Matchers.equalTo(documentId));
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
