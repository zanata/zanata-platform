package org.fedorahosted.flies.repository.model;

import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.hibernate.validator.NotNull;

@MappedSuperclass
public class AbstractTextUnitTarget extends AbstractTextUnit {

	private Document document;
	private TextUnit template;
	private FliesLocale locale;

	public static enum Status {
		New, FuzzyMatch, ForReview, Approved
	}

	private Status status = Status.New;

	private DocumentTarget documentTarget;

	private List<TextUnitCandidate> candidates;

	public AbstractTextUnitTarget() {
	}

	public AbstractTextUnitTarget(Document document, TextUnit template,
			FliesLocale locale) {
		super(document.getRevision());
		this.document = document;
		this.template = template;
		this.locale = locale;
	}

	public AbstractTextUnitTarget(AbstractTextUnitTarget target) {
		super(target);
		this.document = target.document;
		this.template = target.template;
		this.locale = target.locale;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "template_id")
	// @NaturalId
	public TextUnit getTemplate() {
		return template;
	}

	public void setTemplate(TextUnit template) {
		this.template = template;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "document_id")
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "locale_id")
	// @NaturalId
	public FliesLocale getLocale() {
		return locale;
	}

	public void setLocale(FliesLocale locale) {
		this.locale = locale;
	}

	@ManyToOne
	@JoinColumns( {
			@JoinColumn(name = "document_id", referencedColumnName = "document_id", insertable = false, updatable = false),
			@JoinColumn(name = "locale_id", referencedColumnName = "locale_id", insertable = false, updatable = false) })
	public DocumentTarget getDocumentTarget() {
		return documentTarget;
	}

	public void setDocumentTarget(DocumentTarget documentTarget) {
		this.documentTarget = documentTarget;
	}

	@NotNull
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@OneToMany(mappedBy = "target")
	public List<TextUnitCandidate> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<TextUnitCandidate> candidates) {
		this.candidates = candidates;
	}

	/**
	 * Checks if this target corresponds to the current version of the template
	 * 
	 * @return true if this target corresponds to the current version of the
	 *         template
	 */
	@Transient
	public boolean isCurrent() {
		return this.getTemplate().getDocumentRevision() == this
				.getDocumentRevision();
	}

}
