package org.fedorahosted.flies.model;

import java.util.Date;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;

public interface ITextFlowTargetHistory {

	public static enum AuditField{
		POS,REVISION,OBSOLETE,CONTENT;
	}

	ContentState getState();

	Integer getTextFlowRevision();

	HPerson getLastModifiedBy();

	String getContent();
	
	Date getLastChanged();

	Integer getVersionNum();
	
}
