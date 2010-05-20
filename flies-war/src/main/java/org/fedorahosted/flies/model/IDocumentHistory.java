package org.fedorahosted.flies.model;

import java.util.Date;

import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;

public interface IDocumentHistory {

	HPerson getLastModifiedBy();

	Date getLastChanged();

	Integer getRevision();
	
	boolean isObsolete();

	LocaleId getLocale();
	
	ContentType getContentType();
	
	String getName();
	
	String getPath();
	
}
