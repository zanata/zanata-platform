package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CaptionPanel extends DecoratorPanel {

	private VerticalPanel vPanel;
	
	public CaptionPanel() {
		// init panel
		vPanel = new VerticalPanel();
		vPanel.setBorderWidth(0);
		vPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		vPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		vPanel.setSpacing(10);
		vPanel.setWidth("150px");
	}
	
	public void add(Widget widget) {
		vPanel.add(new Label("Documents"));
		vPanel.add(widget);
		super.add(vPanel);
	}
}
