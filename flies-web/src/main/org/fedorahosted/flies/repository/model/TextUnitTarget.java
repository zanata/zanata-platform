package org.fedorahosted.flies.repository.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.fedorahosted.flies.core.model.FliesLocale;

@Entity
public class TextUnitTarget extends AbstractTextUnitTarget {

	private List<TextUnitTargetHistory> history;

	public TextUnitTarget() {
	}

	public TextUnitTarget(TextUnit template, FliesLocale locale) {
		super(template.getDocument(), template, locale);
	}
	@OneToMany
	@JoinColumns( {
			@JoinColumn(name = "document_id", referencedColumnName = "document_id", insertable = false, updatable = false),
			@JoinColumn(name = "template_id", referencedColumnName = "template_id", insertable = false, updatable = false),
			@JoinColumn(name = "locale_id", referencedColumnName = "locale_id", insertable = false, updatable = false) })
	public List<TextUnitTargetHistory> getHistory() {
		return history;
	}

	public void setHistory(List<TextUnitTargetHistory> history) {
		this.history = history;
	}

}
