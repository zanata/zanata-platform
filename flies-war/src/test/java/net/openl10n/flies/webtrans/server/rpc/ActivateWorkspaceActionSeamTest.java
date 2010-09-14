package net.openl10n.flies.webtrans.server.rpc;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import net.openl10n.flies.FliesDBUnitSeamTest;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.webtrans.server.SeamDispatch;
import net.openl10n.flies.webtrans.shared.model.ProjectIterationId;
import net.openl10n.flies.webtrans.shared.model.WorkspaceId;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceAction;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceResult;

import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.Test;

// FIXME fix broken test
@Test(enabled = false, groups = { "seam-tests" })
public class ActivateWorkspaceActionSeamTest extends FliesDBUnitSeamTest
{

   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   // FIXME fix broken test
   @Test(enabled = false)
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