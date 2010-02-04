package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.TransMemory;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class MemoryView extends FlowPanel{
	
	private Label source;
	private Label target;
	
	public MemoryView(TransMemory memory) {
		source = new Label("Source: "+memory.getSource());
		target = new Label("Target: "+memory.getMemory());
		add(source);
		add(target);
	}
	
	public void setSource(Label source) {
		this.source = source;
	}
	public Label getSource() {
		return source;
	}

	public void setTarget(Label target) {
		this.target = target;
	}
	public Label getTarget() {
		return target;
	}
}
