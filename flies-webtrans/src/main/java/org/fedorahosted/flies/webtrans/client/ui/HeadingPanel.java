package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class HeadingPanel extends HorizontalPanel {

	public HeadingPanel() {
		setStyleName("gwt-HeadingPanel");
	}
	
	public HeadingPanel(Widget widget) {
		this();
	}
	
	public void add(String title) {
		this.add(new Label(title));
		addCollapser(); 
	}
	
	private void addCollapser() {
		add(new Button("V"));
	}

	public void setHeadingWidget(Widget widget) {
		// going to be removed
	}
	
	public Widget getWidget() {
		return this;
	}
}
