package org.fedorahosted.flies.webtrans;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.core.model.ProjectTarget;

final class WorkspaceKey{
	
	private final ProjectTarget projectTarget;
	private final FliesLocale locale;
	
	public WorkspaceKey(ProjectTarget projectTarget, FliesLocale locale){
		if(projectTarget == null)
			throw new NullPointerException("projectTarget cannot be null");
		if(locale == null)
			throw new NullPointerException("locale cannot be null");
		
		this.projectTarget = projectTarget;
		this.locale = locale;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if( !(obj instanceof WorkspaceKey) ) return false;
		WorkspaceKey other = (WorkspaceKey) obj;
		return ( other.locale.getId().equals(locale.getId()) 
				&& other.projectTarget.getId().equals(projectTarget.getId()));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.getId().hashCode();
	    hash = hash * 31 + projectTarget.getId().hashCode();
	    return hash;
	}
	
}
