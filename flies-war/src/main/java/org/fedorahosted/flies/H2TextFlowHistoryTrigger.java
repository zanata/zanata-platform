package org.fedorahosted.flies;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.h2.tools.TriggerAdapter;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

public class H2TextFlowHistoryTrigger extends TriggerAdapter { 

	private static Log log = Logging.getLog(H2TextFlowHistoryTrigger.class);
		
	@Override
	public void fire(Connection conn, ResultSet oldRow, ResultSet newRow)
			throws SQLException {

		log.debug("Executing HTextFlowHistory trigger");

		int oldRev = oldRow.getInt("revision");
		int newRev = newRow.getInt("revision"); 
		if ( oldRev != newRev ) {

			log.debug("revision incremented from {0} to {1}. Executing trigger..", oldRev, newRev);
			
			PreparedStatement prep = conn
					.prepareStatement("INSERT INTO HTextFlowHistory (tf_id,revision,content, obsolete, pos) VALUES (?,?,?,?,?)");
			
			prep.setObject(1, oldRow.getObject("id"));
			prep.setObject(2, oldRow.getObject("revision"));
			prep.setObject(3, oldRow.getObject("content"));
			prep.setObject(4, oldRow.getObject("obsolete"));
			prep.setObject(5, oldRow.getObject("pos"));
			prep.execute();
		}
		else {
			log.warn("HTextFlow updated without incrementing revision... skipping tirgger");
		}
		
	}
	
}
