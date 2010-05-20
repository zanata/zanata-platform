package org.fedorahosted.flies.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.hibernate.search.ContentStateBridge;
import org.fedorahosted.flies.hibernate.search.LocaleIdBridge;
import org.fedorahosted.flies.model.type.LocaleIdType;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
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
public class HTextFlowTarget extends AbstractFliesEntity {

	private static final long serialVersionUID = 302308010797605435L;

	private HTextFlow textFlow;
	private LocaleId locale;
	
	private String content;
	private ContentState state = ContentState.New;
	private Integer textFlowRevision;
	private HPerson lastModifiedBy;
	
	private HSimpleComment comment;
	
	public HTextFlowTarget() {
	}
	
	public HTextFlowTarget(HTextFlow textFlow, LocaleId locale) {
		this.locale = locale;
		this.textFlow = textFlow;
		this.textFlowRevision = textFlow.getRevision();
	}
	
	public HTextFlowTarget(TextFlowTarget target) {
		this.content = target.getContent();
		this.locale = target.getLang();
		this.textFlowRevision = target.getResourceRevision();
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
	}
	
	@NaturalId
	@Type(type="localeId")
	@NotNull
	@Field(index=Index.UN_TOKENIZED)
	@FieldBridge(
			impl=LocaleIdBridge.class
	)
	public LocaleId getLocale() {
		return locale;
	}
	
	public void setLocale(LocaleId locale) {
		this.locale = locale;
	}
	
	@NotNull
	@Field(index=Index.UN_TOKENIZED)
	@FieldBridge(
			impl=ContentStateBridge.class
	)
	public ContentState getState() {
		return state;
	}
	
	public void setState(ContentState state) {
		this.state = state;
	}

	@NotNull
	@Column(name="tf_revision")
	public Integer getTextFlowRevision() {
		return textFlowRevision;
	}
	
	public void setTextFlowRevision(Integer textFlowRevision) {
		this.textFlowRevision = textFlowRevision;
	}
	
	@ManyToOne
	@JoinColumn(name="last_modified_by_id", nullable=true)
	public HPerson getLastModifiedBy() {
		return lastModifiedBy;
	}
	
	public void setLastModifiedBy(HPerson lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	
	@NaturalId
	@ManyToOne
	@JoinColumn(name="tf_id")
	@IndexedEmbedded(depth=2)
	public HTextFlow getTextFlow() {
		return textFlow;
	}
	
	public void setTextFlow(HTextFlow textFlow) {
		this.textFlow = textFlow;
//		setResourceRevision(textFlow.getRevision());
	}

	@NotNull
	@Type(type = "text")
//	@Field(index=Index.NO) // no searching on target text yet
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@OneToOne(optional=true, cascade=CascadeType.ALL)
	@JoinColumn(name="comment_id")
	public HSimpleComment getComment() {
		return comment;
	}
	
	public void setComment(HSimpleComment comment) {
		this.comment = comment;
	}
	
	/**
	 * Used for debugging
	 */
	@Override
	public String toString() {
		return "HTextFlowTarget(" +
			"content:"+getContent()+
			"locale:"+getLocale()+
			"state:"+getState()+
			"comment:"+getComment()+
			"textflow:"+getTextFlow().getContent()+
			")";
	}
}
