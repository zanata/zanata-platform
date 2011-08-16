package org.zanata.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jboss.seam.security.Identity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HProject;

@Test(groups = { "jpa-tests" })
public class ProjectDAOTest extends ZanataDbunitJpaTest
{

   private ProjectDAO dao;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeClass
   void beforeClass()
   {
      Identity.setSecurityEnabled(false);
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
