package org.zanata.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.Cleanup;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;

@Test(groups = { "jpa-tests" })
public class WrappedConnectionProviderTest extends ZanataDbunitJpaTest
{

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
      session = getSession();
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
         concurrentResultSetNotDetected();
      }
      catch (JDBCException e)
      {
         checkExceptionType(e);
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
         concurrentResultSetNotDetected();
      }
      catch (JDBCException e)
      {
         checkExceptionType(e);
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
         concurrentResultSetNotDetected();
      }
      catch (JDBCException e)
      {
         checkExceptionType(e);
      }
   }

   private void concurrentResultSetNotDetected()
   {
      Assert.fail("Failed to detect concurrent ResultSet - is Wrapped*ConnectionProvider enabled in persistence.xml?");
   }

   private void checkExceptionType(JDBCException e)
   {
      if (!(e.getSQLException() instanceof StreamingResultSetSQLException))
      {
         throw e;
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
   
}
