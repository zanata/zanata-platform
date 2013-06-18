package org.zanata.feature;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
//@Test(groups = "manual-run", description = "When you use cargo.wait and want to manually run functional tests, this class should be used to do things.")
@Slf4j
public class ManualRunHelper
{
   private static final Pattern TAG_PATTERN = Pattern.compile("\\s*<.+>(.+)</.+>\\s*");
   private static String url;
   private static String driver = "org.h2.Driver";
   private static String username;
   private static String password = "";
   private static Connection connection;
   private static Statement statement;

   @BeforeClass
   public static void setUpDatabase() throws Exception
   {
      extractConnectionInfo();
      Class.forName(driver);
      connection = DriverManager.getConnection(url, username, password);
      statement = connection.createStatement();
   }

   @AfterClass
   public static void cleanUp()
   {
      try
      {
         if (statement != null)
         {
            statement.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }
      catch (SQLException e)
      {
         log.error("error", e);
      }
   }

   private static void extractConnectionInfo() throws IOException
   {
      // poor man's xml parsing
      List<String> lines = readLines("datasource/zanata-ds.xml");
      for (String line : lines)
      {
         Matcher matcher = TAG_PATTERN.matcher(line);
         if (!matcher.matches())
         {
            continue;
         }
         String tagValue = matcher.group(1);
         if (line.contains("<connection-url>"))
         {
            url = tagValue;
         }
         // in as7 there is no longer a driver class tag. Instead there is a driver tag points to a module.
         if (line.contains("<driver-class>"))
         {
            driver = tagValue;
         }
         if (line.contains("<user-name>"))
         {
            username = tagValue;
         }
      }
      log.info("driver: {}, url: {}, username: {}, password: {}", driver, url, username, password);
   }

   private static List<String> readLines(String relativeFilePath) throws IOException
   {
      URL dataSourceFile = Thread.currentThread().getContextClassLoader().getResource(relativeFilePath);
      return Files.readLines(new File(dataSourceFile.getPath()), Charset.defaultCharset());
   }

   @Test
   public void addAdminToDB() throws Exception
   {
      ResultSet resultSet = statement.executeQuery("select count(*) from HAccount where username = 'admin'");
      resultSet.next();
      int adminUser = resultSet.getInt(1);
      if (adminUser == 1)
      {
         log.info("user [admin] already exists. ignored.");
      }
      else
      {
         runScript("create_admin_user.sql");
      }
   }

   @Test
   public void addTranslatorToDB() throws Exception
   {
      ResultSet resultSet = statement.executeQuery("select count(*) from HAccount where username = 'translator'");
      resultSet.next();
      int translator = resultSet.getInt(1);
      if (translator == 1)
      {
         log.info("user [translator] already exists. ignored.");
      }
      else
      {
         runScript("create_translator_user.sql");
      }
   }

   private static void runScript(String scriptName) throws Exception
   {
      List<String> scripts = readLines(scriptName);

      for (String script : scripts)
      {
         if (!script.startsWith("--") && !Strings.isNullOrEmpty(script))
         {
            statement.execute(script);
         }
      }
   }

   @Test
   public void deleteAllVersionGroup() throws SQLException
   {
      statement.executeUpdate("delete from HIterationGroup_ProjectIteration");
      statement.executeUpdate("delete from HIterationGroup_Maintainer");
      statement.executeUpdate("delete from HIterationGroup");
   }

   @Test
   public void deleteProjectAndVersion() throws SQLException
   {
      statement.executeUpdate("delete from HProject_Maintainer");
      statement.executeUpdate("delete from HProjectIteration");
      statement.executeUpdate("delete from HProject");
   }
}
