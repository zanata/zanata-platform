package org.fedorahosted.flies.webtrans.server.rpc;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.ProjectIterationId;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.webtrans.server.SeamDispatch;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

@Test(groups={"seam-tests"})
public class ActivateWorkspaceActionSeamTest extends DBUnitSeamTest {

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

    @Test
    public void activateWorkspaceWithValidData() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
            	SeamDispatch seamDispatch = (SeamDispatch) getInstance(SeamDispatch.class);
            	
            	ActivateWorkspaceAction action = new ActivateWorkspaceAction(
            			new WorkspaceId( new ProjectIterationId("sample-project", "1.0"), new LocaleId("en-US")));
            	ActivateWorkspaceResult result = seamDispatch.execute( action );

            	assertThat(result, notNullValue() );
            }
        }.run();
    }
    
}