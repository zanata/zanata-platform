package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class HeadingPanel extends HorizontalPanel {

	private final Label title;
	private final Button collapseButton;
	
	public HeadingPanel() {
		setStyleName("gwt-HeadingPanel");
		title = new Label();
		collapseButton = new Button("v");
		
		add(title);
		add(collapseButton);
		
		setWidth("100%");
		setCellVerticalAlignment(title, HorizontalPanel.ALIGN_MIDDLE);
		
		setCellHorizontalAlignment(collapseButton, HorizontalPanel.ALIGN_RIGHT);
	}
	
	public void setTitle(String title) {
		this.title.setText(title);
	}
	
	public void setCollapser(boolean collapser) {
		collapseButton.setVisible(collapser);
	}
	
}
