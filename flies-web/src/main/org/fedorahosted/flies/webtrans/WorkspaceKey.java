package org.fedorahosted.flies.webtrans;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.core.model.ProjectIteration;

final class WorkspaceKey{
	
	private final ProjectIteration projectIteration;
	private final FliesLocale locale;
	
	public WorkspaceKey(ProjectIteration projectIteration, FliesLocale locale){
		if(projectIteration == null)
			throw new NullPointerException("projectIteration cannot be null");
		if(locale == null)
			throw new NullPointerException("locale cannot be null");
		
		this.projectIteration = projectIteration;
		this.locale = locale;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if( !(obj instanceof WorkspaceKey) ) return false;
		WorkspaceKey other = (WorkspaceKey) obj;
		return ( other.locale.getId().equals(locale.getId()) 
				&& other.projectIteration.getId().equals(projectIteration.getId()));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.getId().hashCode();
	    hash = hash * 31 + projectIteration.getId().hashCode();
	    return hash;
	}
	
}
