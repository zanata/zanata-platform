package org.fedorahosted.flies.webtrans;

import net.openl10n.adapters.LocaleId;
import net.openl10n.packaging.jpa.project.HProject;

import org.fedorahosted.flies.core.model.ProjectIteration;

final class WorkspaceKey{
	
	private final HProject project;
	private final LocaleId locale;
	
	public WorkspaceKey(HProject project, LocaleId locale){
		if(project == null)
			throw new IllegalArgumentException("project");
		if(locale == null)
			throw new IllegalArgumentException("locale");
		
		this.project = project;
		this.locale = locale;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if( !(obj instanceof WorkspaceKey) ) return false;
		WorkspaceKey other = (WorkspaceKey) obj;
		return ( other.locale.equals(locale) 
				&& other.project.getId().equals(project.getId()));
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash = hash * 31 + locale.hashCode();
	    hash = hash * 31 + project.getId().hashCode();
	    return hash;
	}
	
}
