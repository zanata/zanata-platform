package org.zanata.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import lombok.Cleanup;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.util.CloseableIterator;

import com.google.common.collect.Iterators;

@Test(groups = { "jpa-tests" })
public class TextFlowStreamDAOTest extends ZanataDbunitJpaTest
{

   private ProjectDAO projectDao;
   private ProjectIterationDAO projectIterDao;
   private TextFlowStreamDAO dao;
   private Session session;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      dao = new TextFlowStreamDAO((HibernateEntityManagerFactory) getEmf());
      session = getSession();
      projectDao = new ProjectDAO(session);
      projectIterDao = new ProjectIterationDAO(session);
   }

   @Test
   public void findAllTextFlows() throws Exception
   {
      @Cleanup
      CloseableIterator<HTextFlow> iter = dao.findTextFlows();
      assertThat(Iterators.size(iter), equalTo(5));
   }

   @Test
   public void findTextFlowsForProject() throws Exception
   {
      HProject proj = projectDao.getBySlug("sample-project");
      @Cleanup
      CloseableIterator<HTextFlow> iter = dao.findTextFlowsByProject(proj);
      assertThat(Iterators.size(iter), equalTo(5));
   }

   @Test
   public void findTextFlowsForEmptyProject() throws Exception
   {
      HProject proj = projectDao.getBySlug("retired-project");
      @Cleanup
      CloseableIterator<HTextFlow> iter = dao.findTextFlowsByProject(proj);
      assertThat(iter.hasNext(), Matchers.not(true));
   }
   
   @Test
   public void findTextFlowsForProjectIter() throws Exception
   {
      HProjectIteration projIter = projectIterDao.getBySlug("sample-project", "1.0");
      @Cleanup
      CloseableIterator<HTextFlow> iter = dao.findTextFlowsByProjectIteration(projIter);
      assertThat(Iterators.size(iter), equalTo(5));
   }

   @Test
   public void findTextFlowsForEmptyProjectIteration() throws Exception
   {
      HProjectIteration projIter = projectIterDao.getBySlug("retired-project", "retired-current");
      @Cleanup
      CloseableIterator<HTextFlow> iter = dao.findTextFlowsByProjectIteration(projIter);
      assertThat(iter.hasNext(), Matchers.not(true));
   }
   
}
