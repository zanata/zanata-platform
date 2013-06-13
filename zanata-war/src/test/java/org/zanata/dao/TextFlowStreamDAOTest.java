package org.zanata.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.jdbc.Work;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.jdbc.StreamingResultSetSQLException;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.util.CloseableIterator;

@Test(groups = { "jpa-tests" })
@Slf4j
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
   public void testWrapperWithNestedExecute() throws Exception
   {
      @Cleanup
      ScrollableResults scroll1 = streamQuery("from HTextFlow");
      try
      {
         session.doWork(new Work()
         {
            @Override
            public void execute(Connection connection) throws SQLException
            {
               Statement statement = connection.createStatement();
               statement.executeUpdate("delete from HTextFlow where 0=1");
            }
         });
         Assert.fail("Failed to detect concurrent ResultSet - is WrappedConnectionProvider enabled?");
      }
      catch (JDBCException e)
      {
         if (!(e.getSQLException() instanceof StreamingResultSetSQLException))
         {
            throw e;
         }
      }
   }

   @Test
   public void testWrapperWithNestedStreaming() throws Exception
   {
      @Cleanup
      ScrollableResults scroll1 = streamQuery("from HTextFlow");
      try
      {
         @Cleanup
         ScrollableResults scroll2 = streamQuery("from HTextFlowTarget");
         Assert.fail("Failed to detect concurrent ResultSet - is WrappedConnectionProvider enabled?");
      }
      catch (JDBCException e)
      {
         if (!(e.getSQLException() instanceof StreamingResultSetSQLException))
         {
            throw e;
         }
      }
   }

   @Test
   public void testWrapperWithNestedResults() throws Exception
   {
      @Cleanup
      ScrollableResults scroll1 = streamQuery("from HTextFlow");
      try
      {
         @Cleanup
         ScrollableResults scroll2 = scrollQuery("from HTextFlowTarget");
         Assert.fail("Failed to detect concurrent ResultSet - is WrappedConnectionProvider enabled?");
      }
      catch (JDBCException e)
      {
         if (!(e.getSQLException() instanceof StreamingResultSetSQLException))
         {
            throw e;
         }
      }
   }

   private ScrollableResults streamQuery(String queryString)
   {
      Query q = session.createQuery(queryString);
      q.setFetchSize(Integer.MIN_VALUE);
      ScrollableResults scroll = q.scroll(ScrollMode.FORWARD_ONLY);
      return scroll;
   }

   private ScrollableResults scrollQuery(String queryString)
   {
      Query q = session.createQuery(queryString);
      ScrollableResults scroll = q.scroll();
      return scroll;
   }

   @Test
   public void findAllTextFlows() throws Exception
   {
      @Cleanup
      CloseableIterator<HTextFlow> iter = dao.findTextFlows();
      int n = 0;
      while (iter.hasNext())
      {
         iter.next();
         ++n;
      }
      assertThat(n, equalTo(5));
   }

   @Test
   public void findTextFlowsForProject() throws Exception
   {
      HProject proj = projectDao.getBySlug("sample-project");
      @Cleanup
      CloseableIterator<HTextFlow> iter = dao.findTextFlowsByProject(proj);
      int n = 0;
      while (iter.hasNext())
      {
         iter.next();
         ++n;
      }
      assertThat(n, equalTo(5));
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
      int n = 0;
      while (iter.hasNext())
      {
         iter.next();
         ++n;
      }
      assertThat(n, equalTo(5));
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
