package net.openl10n.flies;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.tools.TriggerAdapter;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

public class H2DocumentHistoryTrigger extends TriggerAdapter
{

   private static Log log = Logging.getLog(H2DocumentHistoryTrigger.class);

   @Override
   public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException
   {

      log.debug("Executing HDocumentHistory trigger");

      int oldRev = oldRow.getInt("revision");
      int newRev = newRow.getInt("revision");
      if (oldRev != newRev)
      {

         log.debug("revision incremented from {0} to {1}. Executing trigger..", oldRev, newRev);

         PreparedStatement prep = conn.prepareStatement("INSERT INTO HDocumentHistory (document_id,revision,contentType,docId,locale,name,path,lastChanged,last_modified_by_id,obsolete) VALUES (?,?,?,?,?,?,?,?,?,?)");

         prep.setObject(1, oldRow.getObject("id"));
         prep.setObject(2, oldRow.getObject("revision"));
         prep.setObject(3, oldRow.getObject("contentType"));
         prep.setObject(4, oldRow.getObject("docId"));
         prep.setObject(5, oldRow.getObject("locale"));
         prep.setObject(6, oldRow.getObject("name"));
         prep.setObject(7, oldRow.getObject("path"));
         prep.setObject(8, oldRow.getObject("lastChanged"));
         prep.setObject(9, oldRow.getObject("last_modified_by_id"));
         prep.setObject(10, oldRow.getObject("obsolete"));
         prep.execute();
      }
      else
      {
         log.warn("HDocument updated without incrementing revision... skipping trigger");
      }

   }

}
