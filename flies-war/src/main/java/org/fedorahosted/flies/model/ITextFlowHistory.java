package org.fedorahosted.flies.model;

public interface ITextFlowHistory {

	public static enum AuditField{
		POS,REVISION,OBSOLETE,CONTENT;
	}
	
	Integer getPos();

	Integer getRevision();

	boolean isObsolete();

	String getContent();

	
	
}
