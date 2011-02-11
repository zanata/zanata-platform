package net.openl10n.flies.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import net.openl10n.flies.FliesDbunitJpaTest;
import net.openl10n.flies.model.HProject;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jboss.seam.security.Identity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "jpa-tests" })
public class ProjectDAOTest extends FliesDbunitJpaTest
{

   private ProjectDAO dao;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
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
