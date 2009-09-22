package org.fedorahosted.flies.repository.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.hibernate.annotations.IndexColumn;
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
public class HTextFlow extends HResource {

	private static final long serialVersionUID = 3023080107971905435L;

	private String content;
	private List<HInlineMarker> markers;
	private List<HTextSegment> segments;
	
	private Map<LocaleId, HTextFlowTarget> targets;

	public Map<Integer, HTextFlowHistory> history;
	
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
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="textFlow")
	@IndexColumn(name="pos")
	public List<HInlineMarker> getMarkers() {
		return markers;
	}
	
	public void setMarkers(List<HInlineMarker> markers) {
		this.markers = markers;
	}
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="textFlow")
	@IndexColumn(name="start")
	public List<HTextSegment> getSegments() {
		return segments;
	}
	
	public void setSegments(List<HTextSegment> segments) {
		this.segments = segments;
	}
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="textFlow")
	@MapKey(name="revision")
	public Map<Integer, HTextFlowHistory> getHistory() {
		return history;
	}
	
	public void setHistory(Map<Integer, HTextFlowHistory> history) {
		this.history = history;
	}
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="textFlow")
	@MapKey(name="locale")
	public Map<LocaleId, HTextFlowTarget> getTargets() {
		if(targets == null) 
			targets = new HashMap<LocaleId, HTextFlowTarget>(); 
		return targets;
	}
	
	public void setTargets(Map<LocaleId, HTextFlowTarget> targets) {
		this.targets = targets;
	}
	
	@Override
	public TextFlow toResource(Set<LocaleId> includedTargets, int levels) {
		TextFlow textFlow = new TextFlow(this.getResId());
		textFlow.setContent(this.getContent());
		textFlow.setLang(this.getDocument().getLocale());
		
		for (LocaleId locale : includedTargets) {
			HTextFlowTarget hTextFlowTarget = this.getTargets().get(locale);
			if(hTextFlowTarget != null) {
				TextFlowTarget textFlowTarget = new TextFlowTarget(textFlow, locale);
				textFlowTarget.setContent(hTextFlowTarget.getContent());
				textFlowTarget.setState(hTextFlowTarget.getState());
				textFlow.addTarget(textFlowTarget);
			}
		}
		return textFlow;
	}
	
}
