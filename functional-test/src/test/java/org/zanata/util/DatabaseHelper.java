package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.io.Files;

import lombok.extern.slf4j.Slf4j;

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
      URL dataSourceFile = Thread.currentThread().getContextClassLoader().getResource(relativeFilePath);
      return Files.readLines(new File(dataSourceFile.getPath()), Charset.defaultCharset());
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
      wrapInTryCatch(new Command()
      {
         @Override
         public void execute() throws Exception
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
      });
   }

   public void addTranslatorUser()
   {
      wrapInTryCatch(new Command()
      {

         @Override
         public void execute() throws Exception
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
      });
   }

   public void addLanguage(final String localeId)
   {
      wrapInTryCatch(new Command()
      {
         @Override
         public void execute() throws Exception
         {
            ResultSet resultSet = statement.executeQuery("select id from HLocale where localeId = '" + localeId + "'");
            if (!resultSet.next())
            {
               statement.executeUpdate("INSERT INTO HLocale (CREATIONDATE,LASTCHANGED,VERSIONNUM,ACTIVE,LOCALEID,ENABLEDBYDEFAULT) VALUES (now(),now(),0,'true','" + localeId + "','true')");
               resultSet = statement.executeQuery("select id from HLocale where localeId = '" + localeId + "'");
               resultSet.next();
            }
            int hLocaleKey = resultSet.getInt("id");

            statement.executeUpdate("INSERT INTO HLocale_Member (PERSONID,SUPPORTEDLANGUAGEID,ISCOORDINATOR) VALUES (1," + hLocaleKey + ",'false')"); // admin
            statement.executeUpdate("INSERT INTO HLocale_Member (PERSONID,SUPPORTEDLANGUAGEID,ISCOORDINATOR) VALUES (2," + hLocaleKey + ",'false')"); // translator
         }
      });
   }

   public void resetData()
   {
      executeQuery("RUNSCRIPT FROM '" + backupPath + "' CHARSET 'UTF-8'");
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
