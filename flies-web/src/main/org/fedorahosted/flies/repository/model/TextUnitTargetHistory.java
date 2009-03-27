package org.fedorahosted.flies.repository.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "document_id",
		"template_id", "locale_id", "document_revision" }) })
public class TextUnitTargetHistory extends AbstractTextUnitTarget {

	public TextUnitTargetHistory() {
	}
	
	public TextUnitTargetHistory(AbstractTextUnitTarget target) {
		super(target);
	}
	
}
