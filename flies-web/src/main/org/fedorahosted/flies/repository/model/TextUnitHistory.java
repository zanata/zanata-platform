package org.fedorahosted.flies.repository.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "resource_id",
		"document_id", "document_revision" }) })
public class TextUnitHistory extends AbstractTextUnitTemplate {

	private TextUnit currentVersion;

	@ManyToOne
	@JoinColumns( {
			@JoinColumn(name = "resource_id", referencedColumnName = "resource_id", insertable = false, updatable = false),
			@JoinColumn(name = "document_id", referencedColumnName = "document_id", insertable = false, updatable = false) })
	public TextUnit getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(TextUnit currentVersion) {
		this.currentVersion = currentVersion;
	}

}
