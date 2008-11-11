package org.fedorahosted.flies.entity.resources;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class DocumentTemplate implements Serializable{

	Long id;
	
	List<DocumentTarget> targets;
	List<TextFlowTemplate> entryTemplates;

	@Id
	public Long getId() {
		return id;
	}
	
	private void setId(Long id) {
		this.id = id;
	}
	
	
	@OneToMany(mappedBy="template")
	public List<DocumentTarget> getTargets() {
		return targets;
	}
	
	public void setTargets(List<DocumentTarget> targets) {
		this.targets = targets;
	}
	
	@OneToMany(mappedBy="documentTemplate")
	public List<TextFlowTemplate> getEntryTemplates() {
		return entryTemplates;
	}
	
	public void setEntryTemplates(List<TextFlowTemplate> entryTemplates) {
		this.entryTemplates = entryTemplates;
	}
	
	
}
