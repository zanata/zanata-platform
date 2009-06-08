package org.fedorahosted.flies.repository.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

/**
 * Represents a flow of text that should be processed as a
 * stand-alone structural unit. 
 *
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 *
 */
@Entity
public class TextFlow extends ParentResource {

	private static final long serialVersionUID = 3023080107971905435L;

	private String content;
	private List<InlineMarker> markers;
	private List<TextSegment> segments;
	
	@NotNull
	@Type(type = "text")
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	@OneToMany(mappedBy = "textFlow")
	@OnDelete(action=OnDeleteAction.CASCADE)
	@IndexColumn(name="pos")
	public List<InlineMarker> getMarkers() {
		return markers;
	}
	
	public void setMarkers(List<InlineMarker> markers) {
		this.markers = markers;
	}
	
	@OneToMany(mappedBy = "textFlow")
	@OnDelete(action=OnDeleteAction.CASCADE)
	@IndexColumn(name="start")
	public List<TextSegment> getSegments() {
		return segments;
	}
	
	public void setSegments(List<TextSegment> segments) {
		this.segments = segments;
	}
	
}
