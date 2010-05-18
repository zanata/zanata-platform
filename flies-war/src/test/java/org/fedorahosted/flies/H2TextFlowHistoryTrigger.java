package org.fedorahosted.flies;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.h2.api.Trigger;

public class H2TextFlowHistoryTrigger implements Trigger {

	@Override
	public void close() throws SQLException {
	}

	@Override
	public void fire(Connection conn, Object[] oldRow, Object[] newRow)
			throws SQLException {
		System.out.println("-----------------trigger executed.!!!");
		
		System.out.println(StringUtils.join(oldRow, ";"));
		System.out.println(StringUtils.join(newRow, ";"));
		
		// 0: id
		// 1: content
		// 2: obsolete
		// 3: pos
		// 4: resid
		// 5: revision
		// 6: comment_id
		// 7: document_id
		// 8: potentrydata_id
		
		PreparedStatement prep = conn
				.prepareStatement("INSERT INTO HTextFlowHistory (tf_id,revision,content) VALUES (?,?,?)");
		prep.setLong(1, (Long) oldRow[0]);
		prep.setInt(2, (Integer) oldRow[5]);
		prep.setCharacterStream(3, (Reader) oldRow[1]);
		prep.execute();

	}

	@Override
	public void init(Connection conn, String schemaName, String triggerName,
			String tableName, boolean before, int type) throws SQLException {
	}

	@Override
	public void remove() throws SQLException {
	}

}
