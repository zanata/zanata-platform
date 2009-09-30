package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LeftPanel extends Composite{
	
	private VerticalPanel vPanel = new VerticalPanel();
	
	public LeftPanel() {
		vPanel.setSpacing(4);
		vPanel.add(new Label("test"));
		vPanel.add(new Label("test2"));
		initWidget(vPanel);
	}

}
