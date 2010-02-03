package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.gwt.model.Concept;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class ConceptView extends FlowPanel{
	
	private Label source;
	private Label comment;
	private Label desc;
	private Label target;
	
	public ConceptView(Concept concept) {
		source = new Label("Term: "+concept.getTerm());
		target = new Label("Explain: "+concept.getEntry().getTerm());
		desc = new Label("Description: " + concept.getDesc());
		add(source);
		add(target);
		add(desc);
	}
	
	public void setSource(Label source) {
		this.source = source;
	}
	public Label getSource() {
		return source;
	}
	public void setComment(Label comment) {
		this.comment = comment;
	}
	public Label getComment() {
		return comment;
	}
	public void setDesc(Label desc) {
		this.desc = desc;
	}
	public Label getDesc() {
		return desc;
	}
	public void setTarget(Label target) {
		this.target = target;
	}
	public Label getTarget() {
		return target;
	}
}
