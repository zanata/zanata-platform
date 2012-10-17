package org.zanata;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to extract data out of a real database and put it in DBUnit's xml format.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "manual-tests")
@Slf4j
public class DBUnitDataExtractor
{
   private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

   private void generateTestData(String dataSetName, List<Map.Entry<String, String>> tableNameToQueryMap) throws Exception
   {
      Class.forName("com.mysql.jdbc.Driver");
      Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/zanata", "root", "");
      IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

      QueryDataSet dataSet = new QueryDataSet(connection);

      for (Map.Entry<String, String> entry : tableNameToQueryMap)
      {
         dataSet.addTable(entry.getKey(), entry.getValue());
      }

      log.info("+++++ writing DBUnit data set to : {}", TEMP_DIR);
      FlatDtdDataSet.write(dataSet, new FileOutputStream(new File(TEMP_DIR, dataSetName + ".dbunit.dtd")));
      FlatXmlDataSet.write(dataSet, new FileOutputStream(new File(TEMP_DIR, dataSetName + ".dbunit.xml")));

      jdbcConnection.close();
   }

   @Test
   public void run() throws Exception
   {
      List<Map.Entry<String, String>> tableNameAndQuery = Lists.newArrayList();

      addEntry(tableNameAndQuery, "HAccount", "SELECT * FROM HAccount where id = 1");
      addEntry(tableNameAndQuery, "HPerson", "SELECT * FROM HPerson where id = 1");
      addEntry(tableNameAndQuery, "HLocale", "SELECT * FROM HLocale where id = 3 or id = 5");
      addEntry(tableNameAndQuery, "HPoHeader", "SELECT * FROM HPoHeader where id = 1");
      addEntry(tableNameAndQuery, "HProject", "SELECT * FROM HProject where id = 1");
      addEntry(tableNameAndQuery, "HProjectIteration", "SELECT * FROM HProjectIteration where id = 1");
      addEntry(tableNameAndQuery, "HDocument", "SELECT * FROM HDocument where id = 1");
      addEntry(tableNameAndQuery, "HSimpleComment", "SELECT * FROM HSimpleComment where (id >=2 AND id <=11) or (id >=3947 AND id <= 3949)");
      addEntry(tableNameAndQuery, "HPotEntryData", "SELECT * FROM HPotEntryData where id >= 1 AND id <=10");
      addEntry(tableNameAndQuery, "HTextFlow", "SELECT * FROM HTextFlow where document_id = 1 AND pos < 10");
      addEntry(tableNameAndQuery, "HTextFlowTarget", "SELECT * FROM HTextFlowTarget where tf_id >= 1 AND tf_id <= 10 AND locale = 3");

      generateTestData("GetTransUnitListHandlerPerformanceTest", tableNameAndQuery);
   }

   private static void addEntry(List<Map.Entry<String, String>> tableNameAndQuery, String table, String query)
   {
      tableNameAndQuery.add(new AbstractMap.SimpleEntry<String, String>(table, query));
   }

}
