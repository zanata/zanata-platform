package org.fedorahosted.flies.webtrans;


import org.fedorahosted.flies.common.LocaleId;

final class WorkspaceKey{
	
	private final Long projectContainerId;
	private final LocaleId localeId;
	
	public WorkspaceKey(Long projectContainerId, LocaleId localeId){
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
		if( !(obj instanceof WorkspaceKey) ) return false;
		WorkspaceKey other = (WorkspaceKey) obj;
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
	
	public Long getProjectContainerId() {
		return projectContainerId;
	}
	
}
