package org.fedorahosted.flies.dao;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesDbunitJpaTest;
import org.fedorahosted.flies.model.HProject;
import org.hibernate.Session;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;


@Test(groups = { "jpa-tests" })
public class ProjectDAOTest extends FliesDbunitJpaTest {
	
	@Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("org/fedorahosted/flies/test/model/ProjectData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }
	
	@Test
	public void getValidProjectBySlug() {
		ProjectDAO dao = new ProjectDAO((Session) getEm().getDelegate());
		HProject project = dao.getBySlug("sample-project");
		assertThat(project, notNullValue());
		assertThat(project.getName(), is("Sample Project"));
	}

	
	

}
