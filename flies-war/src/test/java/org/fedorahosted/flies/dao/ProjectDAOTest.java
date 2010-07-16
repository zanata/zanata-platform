package org.fedorahosted.flies.dao;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesDbunitJpaTest;
import org.fedorahosted.flies.model.HProject;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@Test(groups = { "jpa-tests" })
public class ProjectDAOTest extends FliesDbunitJpaTest
{

   private ProjectDAO dao;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      dao = new ProjectDAO((Session) getEm().getDelegate());
   }

   @Test
   public void getValidProjectBySlug()
   {
      HProject project = dao.getBySlug("sample-project");
      assertThat(project, notNullValue());
      assertThat(project.getName(), is("Sample Project"));
   }

   public void getValidProjectById()
   {
      HProject project = dao.findById(1l, false);
      assertThat(project, notNullValue());
      assertThat(project.getName(), is("Sample Project"));
   }

}
