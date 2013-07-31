package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.openqa.selenium.support.ui.FluentWait;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class DatabaseHelper
{
   private static String url;
   private static String driver = "org.h2.Driver";
   private static String username;
   private static String password = "";
   private static Connection connection;
   private static Statement statement;
   private static DatabaseHelper DB;
   private String backupPath;
   private FluentWait<Statement> wait;

   private DatabaseHelper()
   {
      wrapInTryCatch(new Command()
      {
         @Override
         public void execute() throws Exception
         {
            extractConnectionInfo();
            Class.forName(DatabaseHelper.driver);
            connection = DriverManager.getConnection(DatabaseHelper.url, DatabaseHelper.username, DatabaseHelper.password);
            statement = connection.createStatement();
            backupPath = PropertiesHolder.getProperty("zanata.database.backup");
            if (!new File(backupPath).exists())
            {
               // we only want to run this once
               statement.execute("SCRIPT NOPASSWORDS NOSETTINGS DROP TO '" + backupPath + "' CHARSET 'UTF-8' ");
            }
         }
      });
      wait = new FluentWait<Statement>(statement).ignoring(RuntimeException.class, AssertionError.class).pollingEvery(1, TimeUnit.SECONDS).withTimeout(5, TimeUnit.SECONDS).withMessage("running database reset...");
   }

   public static DatabaseHelper database()
   {
      if (DB == null)
      {
         DB = new DatabaseHelper();
      }
      return DB;
   }

   private static void extractConnectionInfo() throws IOException
   {
      // poor man's xml parsing
      List<String> lines = readLines("datasource/zanata-ds.xml");
      Pattern tagPattern = Pattern.compile("\\s*<.+>(.+)</.+>\\s*");
      for (String line : lines)
      {
         Matcher matcher = tagPattern.matcher(line);
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
      File file = getFileFromClasspath(relativeFilePath);
      return Files.readLines(file, Charset.defaultCharset());
   }

   private static File getFileFromClasspath(String relativeFilePath)
   {
      URL dataSourceFile = Thread.currentThread().getContextClassLoader().getResource(relativeFilePath);
      return new File(dataSourceFile.getPath());
   }

   public void runScript(final String scriptFileName)
   {
      wrapInTryCatch(new Command()
      {
         @Override
         public void execute() throws Exception
         {
            List<String> scripts = readLines(scriptFileName);

            for (String script : scripts)
            {
               if (!script.startsWith("--") && !Strings.isNullOrEmpty(script))
               {
                  statement.execute(script);
               }
            }
         }
      });
   }

   private void executeQuery(final String sql)
   {
      wrapInTryCatch(new Command()
      {
         @Override
         public void execute() throws Exception
         {
            statement.execute(sql);
         }
      });
   }

   public void addAdminUser()
   {
      addUserIfNotExist("admin");
   }

   public void addTranslatorUser()
   {
      addUserIfNotExist("translator");
   }

   private void addUserIfNotExist(final String user)
   {
      wrapInTryCatch(new Command()
      {
         @Override
         public void execute() throws Exception
         {

            ResultSet resultSet = statement.executeQuery("select count(*) from HAccount where username = '" + user + "'");
            resultSet.next();
            int adminUser = resultSet.getInt(1);
            if (adminUser == 1)
            {
               log.info("user already exists. ignored.");
            }
            else
            {
               runScript("create_" + user + "_user.sql");
            }
         }
      });
   }

   public void resetDatabaseWithData()
   {
      String path = getFileFromClasspath("org/zanata/feature/zanata_with_data.sql").getAbsolutePath();
      executeQuery("RUNSCRIPT FROM '" + path + "' CHARSET 'UTF-8'");
      waitUntil("select count(*) from HProjectIteration", 1);
   }

   public void resetData()
   {
      executeQuery("RUNSCRIPT FROM '" + backupPath + "' CHARSET 'UTF-8'");
      waitUntil("select count(*) from HProjectIteration", 0);
   }

   public void resetFileData()
   {
      File path = new File(PropertiesHolder.getProperty("document.storage.directory"));
      if(path.exists())
      {
         try {
            FileUtils.deleteDirectory(path);
         }catch (IOException e)
         {
            log.error("Failed to delete", path, e);
            throw new RuntimeException("error");
         }
      }

   }

   private void waitUntil(final String sql, final int expectedResultCount)
   {
      wait.until(new Predicate<Statement>()
      {
         @Override
         public boolean apply(final Statement input)
         {
            wrapInTryCatch(new Command()
            {
               @Override
               public void execute() throws Exception
               {
                  ResultSet resultSet = input.executeQuery(sql);
                  resultSet.next();
                  assertThat(resultSet.getInt(1), Matchers.equalTo(expectedResultCount));
               }
            });
            return true;
         }
      });
   }

   // If we close connection here, in JBoss all the hibernate session won't be usable anymore.
   // This is not good for having cargo running and execute test in IDE.
   private void cleanUp()
   {
      wrapInTryCatch(new Command()
      {
         @Override
         public void execute() throws Exception
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
      });
   }

   // it will be so much nicer to write and read with groovy or java lambda...
   private static void wrapInTryCatch(Command command)
   {
      try
      {
         command.execute();
      }
      catch (Exception e)
      {
         log.error("error", e);
         throw new RuntimeException("error");
      }
   }

   public static interface Command
   {
      public void execute() throws Exception;
   }


}
