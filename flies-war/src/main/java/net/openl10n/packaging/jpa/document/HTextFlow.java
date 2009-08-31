package net.openl10n.packaging.jpa.document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import net.openl10n.api.LocaleId;
import net.openl10n.api.rest.document.TextFlow;

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
public class HTextFlow extends HParentResource {

	private static final long serialVersionUID = 3023080107971905435L;

	private String content;
	private List<HInlineMarker> markers;
	private List<HTextSegment> segments;
	
	private Map<LocaleId, HTextFlowTarget> targets;
	
	public HTextFlow() {
	}
	
	public HTextFlow(TextFlow tf) {
		super(tf);
		this.content = tf.getContent();
	}

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
	public List<HInlineMarker> getMarkers() {
		return markers;
	}
	
	public void setMarkers(List<HInlineMarker> markers) {
		this.markers = markers;
	}
	
	@OneToMany(mappedBy = "textFlow")
	@OnDelete(action=OnDeleteAction.CASCADE)
	@IndexColumn(name="start")
	public List<HTextSegment> getSegments() {
		return segments;
	}
	
	public void setSegments(List<HTextSegment> segments) {
		this.segments = segments;
	}
	
	@OneToMany(mappedBy="textFlow")
	@MapKey(name="locale")
	public Map<LocaleId, HTextFlowTarget> getTargets() {
		if(targets == null) 
			targets = new HashMap<LocaleId, HTextFlowTarget>(); 
		return targets;
	}
	
	public void setTargets(Map<LocaleId, HTextFlowTarget> targets) {
		this.targets = targets;
	}
}
