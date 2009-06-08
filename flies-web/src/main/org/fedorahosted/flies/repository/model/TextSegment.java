package org.fedorahosted.flies.repository.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

@Entity
public class TextSegment implements Serializable{

	private static final long serialVersionUID = 8581001791457747994L;

	private Long id;
	private TextFlow textFlow;
	
	private Integer startPos;
	private Integer endPos;
	
	public TextSegment() {
	}
	
	public TextSegment(TextFlow flow) {
		setTextFlow(flow);
		String content = flow.getContent();
		if(content != null){
			setStartPos(0);
			setEndPos(content.length());
		}
	}
	
	@Id
	public Long getId() {
		return id;
	}
	
	protected void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne
	@JoinColumn(name = "text_flow_id")
	@NotNull
	public TextFlow getTextFlow() {
		return textFlow;
	}
	
	public void setTextFlow(TextFlow textFlow) {
		this.textFlow = textFlow;
	}

	@NotNull
	@Range(min=0)
	public Integer getStartPos() {
		return startPos;
	}

	public void setStartPos(Integer startPos) {
		this.startPos = startPos;
	}
	
	@Range(min=0)
	public Integer getEndPos() {
		return endPos;
	}
	
	public void setEndPos(Integer endPos) {
		this.endPos = endPos;
	}
}
