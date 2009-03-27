package org.fedorahosted.flies.repository.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
public class TextUnit extends AbstractTextUnitTemplate {

	private List<TextUnitTarget> targets;

	private List<TextUnitHistory> history;

	@OneToMany(mappedBy = "template")
	public List<TextUnitTarget> getTargets() {
		return targets;
	}

	public void setTargets(List<TextUnitTarget> targets) {
		this.targets = targets;
	}

	@OneToMany
	@JoinColumns( {
			@JoinColumn(name = "resource_id", referencedColumnName = "resource_id", insertable = false, updatable = false),
			@JoinColumn(name = "document_id", referencedColumnName = "document_id", insertable = false, updatable = false) })
	public List<TextUnitHistory> getHistory() {
		return history;
	}

	public void setHistory(List<TextUnitHistory> history) {
		this.history = history;
	}

}
