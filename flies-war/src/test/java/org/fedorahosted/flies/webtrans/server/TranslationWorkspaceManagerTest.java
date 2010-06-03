package org.fedorahosted.flies.webtrans.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesDbunitJpaTest;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.webtrans.shared.model.ProjectIterationId;
import org.fedorahosted.flies.webtrans.shared.model.WorkspaceId;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TranslationWorkspaceManagerTest  extends FliesDbunitJpaTest {

	private TranslationWorkspaceManager twm;
	
	@Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }


	@BeforeMethod(firstTimeOnly=true)
	public void setup() {
		twm = new TranslationWorkspaceManager((Session) getEm().getDelegate());
	}

	
	@Test
	public void createNewWorkspace() {
		assertThat(twm.getWorkspaceCount(), is(0));
		
		WorkspaceId wsId = new WorkspaceId(
				new ProjectIterationId("sample-project", "1.0"), LocaleId.DE);
		
		TranslationWorkspace tw = twm.getOrRegisterWorkspace(wsId);

		assertThat(twm.getWorkspaceCount(), is(1));
		
		assertThat(tw.getWorkspaceContext().getWorkspaceName(), is("Sample Project (Version 1.0)"));
		
		
	}
}
