package org.fedorahosted.flies.model;

public interface ITextFlowHistory {

	Integer getPos();

	Integer getRevision();

	boolean isObsolete();

	String getContent();

	
	
}
