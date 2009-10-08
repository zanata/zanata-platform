package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CaptionPanel extends DecoratorPanel {

	private VerticalPanel innerPanel;
	
	public CaptionPanel() {
		add(new Label("test"));
	}
	
	
}
