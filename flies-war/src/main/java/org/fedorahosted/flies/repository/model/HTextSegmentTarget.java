package org.fedorahosted.flies.repository.model;

import org.hibernate.validator.NotNull;

//@Entity
public class HTextSegmentTarget {

	private HTextSegment segment;
	private String content;
	
	@NotNull
	public HTextSegment getSegment() {
		return segment;
	}
	public void setSegment(HTextSegment segment) {
		this.segment = segment;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
