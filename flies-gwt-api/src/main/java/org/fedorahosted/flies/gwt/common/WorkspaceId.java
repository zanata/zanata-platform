package org.fedorahosted.flies.gwt.common;


import java.io.Serializable;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public final class WorkspaceId implements Serializable { 

	private static final long serialVersionUID = 1045784401405248038L;

	private ProjectContainerId projectContainerId;
	private LocaleId localeId;
	
	@SuppressWarnings("unused")
	private WorkspaceId() {
	}
	
	public WorkspaceId(ProjectContainerId projectContainerId , LocaleId localeId){
		if(projectContainerId == null)
			throw new IllegalArgumentException("projectContainerId");
		if(localeId == null)
			throw new IllegalArgumentException("localeId");
		
		this.projectContainerId = projectContainerId;
		this.localeId = localeId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if( !(obj instanceof WorkspaceId) ) return false;
		WorkspaceId other = (WorkspaceId) obj;
		return ( other.localeId.equals(localeId) 
				&& other.projectContainerId.equals(projectContainerId));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + localeId.hashCode();
	    hash = hash * 31 + projectContainerId.hashCode();
	    return hash;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}
	
	@Override
	public String toString() {
		return localeId.toString()+":"+projectContainerId;
	}
	
}
