package org.fedorahosted.flies.core.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class RepositoryState {
	
	private Repository repository;
	private Date lastUpdate;
	private Integer lastRevision;

	@OneToOne
	@Id
	public Repository getRepository() {
		return repository;
	}
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	public Date getLastUpdate() {
		return lastUpdate;
	}
	
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public Integer getLastRevision() {
		return lastRevision;
	}
	
	public void setLastRevision(Integer lastRevision) {
		this.lastRevision = lastRevision;
	}
	
}
