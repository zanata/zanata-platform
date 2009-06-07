package org.fedorahosted.flies.repository.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.hibernate.validator.NotNull;

@Entity
public class DocumentTarget implements Serializable {

	private static final long serialVersionUID = 558976374885121435L;

	private Long id;
	private Document template;
	private FliesLocale locale;

	private Integer version;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
//	@JoinColumn(name = "document_id")
	//@NaturalId
	public Document getTemplate() {
		return template;
	}

	public void setTemplate(Document template) {
		this.template = template;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "locale_id")
	//@NaturalId
	public FliesLocale getLocale() {
		return locale;
	}

	public void setLocale(FliesLocale locale) {
		this.locale = locale;
	}

	@Version
	public Integer getVersion() {
		return version;
	}

	private void setVersion(Integer version) {
		this.version = version;
	}

}
