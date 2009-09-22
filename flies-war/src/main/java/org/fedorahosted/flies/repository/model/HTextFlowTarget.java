package org.fedorahosted.flies.repository.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.hibernate.type.LocaleIdType;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTarget.ContentState;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.NotNull;

/**
 * Represents a flow of text that should be processed as a
 * stand-alone structural unit. 
 *
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 *
 */
@Entity
@TypeDef(name="localeId", typeClass=LocaleIdType.class)
public class HTextFlowTarget implements Serializable{

	private static final long serialVersionUID = 302308010797605435L;

	private Long id;
	
	private HTextFlow textFlow;
	private LocaleId locale;
	
	private String content;
	private ContentState state = ContentState.New;
	private Integer revision = 1;
	
	private HSimpleComment comment;
	
	private HDocumentTarget documentTarget; 
	
	public HTextFlowTarget() {
	}
	
	public HTextFlowTarget(HDocumentTarget docTarget, HTextFlow textFlow) {
		if(docTarget.getTemplate() != textFlow.getDocument()){
			throw new IllegalStateException("docTarget and textFlow must reference same Document");
		}
		this.documentTarget = docTarget;
		this.locale = docTarget.getLocale();
		this.textFlow = textFlow;
		this.revision = textFlow.getRevision();
	}
	
	public HTextFlowTarget(TextFlowTarget target) {
		this.content = target.getContent();
		this.locale = target.getLang();
		this.revision = target.getVersion();
		this.state = target.getState();
//		setTextFlow(target.getTextFlow);
//		setComment(target.comment);
//		setDocumentTarget(target.documentTarget);
	}
	
	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}
	
	protected void setId(Long id) {
		this.id = id;
	}
	
	public void copy(TextFlowTarget tfTarget){
		this.content = tfTarget.getContent();
		this.state = tfTarget.getState();
		this.revision = tfTarget.getVersion();
	}
	
	@NaturalId
	@Type(type="localeId")
	@NotNull
	public LocaleId getLocale() {
		return locale;
	}
	
	public void setLocale(LocaleId locale) {
		this.locale = locale;
	}
	
	@NotNull
	public ContentState getState() {
		return state;
	}
	
	public void setState(ContentState state) {
		this.state = state;
	}

	@NotNull
	public Integer getRevision() {
		return revision;
	}
	
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	
	@NaturalId
	@OneToOne
	@JoinColumn(name="resource_id")
	public HTextFlow getTextFlow() {
		return textFlow;
	}
	
	public void setTextFlow(HTextFlow textFlow) {
		this.textFlow = textFlow;
	}

	@ManyToOne
	@JoinColumn(name="document_target_id", nullable=false)
	public HDocumentTarget getDocumentTarget() {
		// (should be the same as textFlow.getDocument().getTargets().get(getLocale());)
		// in other words, this value should never change
		return documentTarget;
	}

	public void setDocumentTarget(HDocumentTarget documentTarget) {
		this.documentTarget = documentTarget;
	}
	
	@NotNull
	@Type(type = "text")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@OneToOne(mappedBy="target", optional=true, cascade=CascadeType.ALL)
	public HSimpleComment getComment() {
		return comment;
	}
	
	public void setComment(HSimpleComment comment) {
		this.comment = comment;
	}

	
}
