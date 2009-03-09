package org.fedorahosted.flies.resources.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.fedorahosted.flies.core.model.FliesLocale;

@Entity
@Table(	uniqueConstraints = {@UniqueConstraint(columnNames={"resource_id", "document_id"})})
public class TextUnit extends AbstractTextUnitTemplate{

	private List<TextUnitTarget> targets;
	
	private List<TextUnitHistory> history;
	
	@OneToMany(mappedBy="template")
	public List<TextUnitTarget> getTargets() {
		return targets;
	}
	
	public void setTargets(List<TextUnitTarget> targets) {
		this.targets = targets;
	}
	
	@OneToMany
	@JoinColumns({
		@JoinColumn(name="resource_id", referencedColumnName="resource_id", insertable=false, updatable=false),
		@JoinColumn(name="document_id", referencedColumnName="document_id", insertable=false, updatable=false)
	})
	public List<TextUnitHistory> getHistory() {
		return history;
	}
	
	public void setHistory(List<TextUnitHistory> history) {
		this.history = history;
	}

}
