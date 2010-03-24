package org.fedorahosted.flies.repository.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.repository.model.po.HPotEntryData;
import org.fedorahosted.flies.repository.model.po.PoUtility;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.po.PotEntryData;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.NotNull;

/**
 * Represents a flow of text that should be processed as a
 * stand-alone structural unit. 
 *
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 *
 */
@Entity
@Indexed
public class HTextFlow extends HDocumentResource {

	private static final long serialVersionUID = 3023080107971905435L;

	private String content;
	private List<HInlineMarker> markers;
	private List<HTextSegment> segments;
	
	private Map<LocaleId, HTextFlowTarget> targets;

	public Map<Integer, HTextFlowHistory> history;
	
	private HSimpleComment comment;
	
	private HPotEntryData potEntryData;
	
	public HTextFlow() {
	}
	
	public HTextFlow(TextFlow tf, int nextDocRev) {
		super(tf, nextDocRev);
		this.content = tf.getContent();
	}

	@OneToOne(optional=true, cascade=CascadeType.ALL)
	@JoinColumn(name="comment_id")
	public HSimpleComment getComment() {
		return comment;
	}
	
	public void setComment(HSimpleComment comment) {
		this.comment = comment;
	}
	
	@NotNull
	@Type(type = "text")
	@Field(index=Index.TOKENIZED)
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
	
	public void setPotEntryData(HPotEntryData potEntryData) {
		this.potEntryData = potEntryData;
	}
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY, optional=true)
	public HPotEntryData getPotEntryData() {
		return potEntryData;
	}
	
	@Override
	public TextFlow toResource(int levels) {
		TextFlow textFlow = new TextFlow(this.getResId());
		HSimpleComment comment = this.getComment();
		if(comment != null) {
			textFlow.getOrAddComment().setValue(comment.getComment());
		}
		textFlow.setContent(this.getContent());
		textFlow.setLang(this.getDocument().getLocale());
		textFlow.setRevision(this.getRevision());
		
		for (LocaleId locale : getTargets().keySet()) {
			HTextFlowTarget hTextFlowTarget = this.getTargets().get(locale);
			if(hTextFlowTarget != null) {
				TextFlowTarget textFlowTarget = new TextFlowTarget(textFlow, locale);
				HSimpleComment tftComment = hTextFlowTarget.getComment();
				if(tftComment != null) {
					textFlowTarget.getOrAddComment().setValue(tftComment.getComment());
				}
				textFlowTarget.setContent(hTextFlowTarget.getContent());
				textFlowTarget.setResourceRevision(hTextFlowTarget.getResourceRevision());
				textFlowTarget.setRevision(hTextFlowTarget.getRevision());
				textFlowTarget.setState(hTextFlowTarget.getState());
				textFlow.addTarget(textFlowTarget);
			}
		}

		HPotEntryData fromHPotEntryData = this.getPotEntryData();
		if (fromHPotEntryData != null) {
			PotEntryData toPotEntryData = textFlow.getOrAddExtension(PotEntryData.class);
			toPotEntryData.setId(this.getResId());
			toPotEntryData.setContext(fromHPotEntryData.getContext());
			HSimpleComment extractedComment = fromHPotEntryData.getExtractedComment();
			toPotEntryData.setExtractedComment(HSimpleComment.toSimpleComment(extractedComment));
			List<String> toFlags = toPotEntryData.getFlags();
			toFlags.addAll(PoUtility.splitFlags(fromHPotEntryData.getFlags()));
			List<String> toReferences = toPotEntryData.getReferences();
			toReferences.addAll(PoUtility.splitRefs(fromHPotEntryData.getReferences()));
		}
		
		return textFlow;
	}
	
}
