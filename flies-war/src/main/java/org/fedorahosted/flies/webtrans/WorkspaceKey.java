package org.fedorahosted.flies.webtrans;

import net.openl10n.packaging.jpa.project.HProject;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.ProjectIteration;

final class WorkspaceKey{
	
	private final Long projectId;
	private final LocaleId locale;
	
	public WorkspaceKey(Long projectId, LocaleId locale){
		if(projectId == null)
			throw new IllegalArgumentException("projectId");
		if(locale == null)
			throw new IllegalArgumentException("locale");
		
		this.projectId = projectId;
		this.locale = locale;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if( !(obj instanceof WorkspaceKey) ) return false;
		WorkspaceKey other = (WorkspaceKey) obj;
		return ( other.locale.equals(locale) 
				&& other.projectId.equals(projectId));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.hashCode();
	    hash = hash * 31 + projectId.hashCode();
	    return hash;
	}
	
}
