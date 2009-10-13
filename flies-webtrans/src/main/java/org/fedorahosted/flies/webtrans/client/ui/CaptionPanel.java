package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CaptionPanel extends DecoratorPanel {

	private VerticalPanel mainPanel;
	private HeadingPanel headPanel;
	private VerticalPanel bodyPanel;
	
	public CaptionPanel() {
		// init panel
		mainPanel = new VerticalPanel();
		mainPanel.setBorderWidth(0);
		mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		mainPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		mainPanel.setSpacing(0);
		mainPanel.setWidth("150px");
		
		// init sub-panel
		headPanel = new HeadingPanel();
		bodyPanel = new VerticalPanel();
	}
	
	public void addHead(String title) {
		headPanel.add(title);
	}
	
	public void addHead(Widget widget) {
		headPanel.add(widget);
		headPanel.add(new Button("^"));
	}
	
	public void addBody(Widget widget) {
		bodyPanel.add(widget);
	}
	
	public void initPanel() {
		mainPanel.add(headPanel);
		mainPanel.add(bodyPanel);
		add(mainPanel);
	}
}
