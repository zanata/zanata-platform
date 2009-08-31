package org.fedorahosted.flies.repository.model.document;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.repository.model.LocaleIdType;
import org.fedorahosted.flies.rest.dto.ContentTarget;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
public class HDocumentTarget implements Serializable {

	private static final long serialVersionUID = 558976374885121435L;

	private Long id;
	private HDocument template;
	private LocaleId locale;
	
	private Set<HTextFlowTarget> targets;

	public HDocumentTarget() {
	}
	
	public HDocumentTarget(HDocument template, LocaleId locale) {
		this.template = template;
		this.locale = locale;
	}
	
	
	public HDocumentTarget(HDocument doc, ContentTarget docTarget) {
		this.template = doc;
		this.locale = docTarget.getTargetLanguage();
	}

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "document_id")
	@NaturalId
	public HDocument getTemplate() {
		return template;
	}

	public void setTemplate(HDocument template) {
		this.template = template;
	}

	@NaturalId
	@Type(type="localeId")
	public LocaleId getLocale() {
		return locale;
	}

	public void setLocale(LocaleId locale) {
		this.locale = locale;
	}

	@OneToMany(mappedBy="documentTarget")
	public Set<HTextFlowTarget> getTargets() {
		return targets;
	}
	
	public void setTargets(Set<HTextFlowTarget> targets) {
		this.targets = targets;
	}
	
}
