package org.fedorahosted.flies.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;

@Entity
public class HTextFlowHistory implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Integer revision;
	private HTextFlow textFlow;
	private String content;

	public HTextFlowHistory() {
	}
	
	public HTextFlowHistory(HTextFlow textFlow) {
		this.revision = textFlow.getRevision();
		this.content = textFlow.getContent();
		this.textFlow = textFlow;
	}
	
	
	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}
	
	
	protected void setId(Long id) {
		this.id = id;
	}
	
	@NaturalId
	public Integer getRevision() {
		return revision;
	}
	
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	
	@NaturalId
	@ManyToOne
	@JoinColumn(name="tf_id")
	public HTextFlow getTextFlow() {
		return textFlow;
	}
	
	public void setTextFlow(HTextFlow textFlow) {
		this.textFlow = textFlow;
	}
	
	@Type(type = "text")
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
