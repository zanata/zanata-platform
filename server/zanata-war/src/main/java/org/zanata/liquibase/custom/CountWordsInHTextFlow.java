package org.zanata.liquibase.custom;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.log.LogFactory;

import org.zanata.util.OkapiUtil;

public class CountWordsInHTextFlow implements CustomTaskChange
{

   @Override
   public String getConfirmationMessage()
   {
      return "CountWordsInHTextFlow updated wordCount column in HTextFlow table";
   }

   @Override
   public void setUp() throws SetupException
   {
   }

   @Override
   public void setFileOpener(FileOpener fileOpener)
   {
   }

   @Override
   public void validate(Database database) throws InvalidChangeDefinitionException
   {
   }

   @Override
   public void execute(Database database) throws CustomChangeException, UnsupportedChangeException
   {
      DatabaseConnection conn = database.getConnection();
      try
      {
         Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
         try
         {
            Map<Long, String> docToLocaleMap = new HashMap<Long, String>();
            String docLocaleSql = "select doc.id, loc.localeId from HDocument doc, HLocale loc where doc.locale = loc.id";
            ResultSet rs1 = stmt.executeQuery(docLocaleSql);
            while (rs1.next())
            {
               long docId = rs1.getLong(1);
               String locale = rs1.getString(2);
               docToLocaleMap.put(docId, locale);
            }
            String countSql = "select count(*) from HTextFlow where wordCount is null or wordCount = 0";
            ResultSet rs2 = stmt.executeQuery(countSql);
            rs2.next();
            long totalRows = rs2.getLong(1);
            Logger log = LogFactory.getLogger();
            log.info("CountWordsInHTextFlow: updating " + totalRows + " rows");
            String textFlowSql = "select id, document_id, content, wordCount from HTextFlow where wordCount is null or wordCount = 0";
            ResultSet rs3 = stmt.executeQuery(textFlowSql);
            long rowsUpdated = 0;
            while (rs3.next())
            {
               // primary key only needed for updatable ResultSet
               rs3.getLong(1);
               long docId = rs3.getLong(2);
               String content = rs3.getString(3);
               String locale = docToLocaleMap.get(docId);
               long wordCount = OkapiUtil.countWords(content, locale);
               rs3.updateLong(4, wordCount);
               rs3.updateRow();
               if (++rowsUpdated % 10000 == 0)
               {
                  log.info("CountWordsInHTextFlow: updated " + rowsUpdated + "/" + totalRows);
               }
            }
            log.info("CountWordsInHTextFlow: finished");
         }
         finally
         {
            stmt.close();
         }
      }
      catch (SQLException e)
      {
         throw new CustomChangeException(e);
      }
      finally
      {
         // conn.close(); ?
      }
   }

}
