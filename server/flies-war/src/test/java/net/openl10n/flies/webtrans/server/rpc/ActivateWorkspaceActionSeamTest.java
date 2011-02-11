package net.openl10n.flies.webtrans.server.rpc;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HIterationProject;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.webtrans.server.SeamDispatch;
import net.openl10n.flies.webtrans.shared.model.ProjectIterationId;
import net.openl10n.flies.webtrans.shared.model.WorkspaceId;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceAction;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

@Test(groups = { "seam-tests" })
public class ActivateWorkspaceActionSeamTest extends DBUnitSeamTest
{

   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Test
   public void activateWorkspaceWithValidData() throws Exception
   {
      new FacesRequest()
      {

         protected void invokeApplication() throws Exception
         {
            SeamDispatch seamDispatch = (SeamDispatch) getInstance(SeamDispatch.class);

            ActivateWorkspaceAction action = new ActivateWorkspaceAction(new WorkspaceId(new ProjectIterationId("sample-project", "1.0"), new LocaleId("en-US")));
            ActivateWorkspaceResult result = seamDispatch.execute(action);

            assertThat(result, notNullValue());
         }
      }.run();
   }

}