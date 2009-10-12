package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class HeadingWidget extends HorizontalPanel {
	
	private final Label headingLabel;
	private final Button collapseButton;
	
	public HeadingWidget() {
		this("");
	}
	
	public HeadingWidget(String label) {
		this.headingLabel = new Label(label);
		this.collapseButton = new Button("X");
		setSize("100%", "100%");
		add(headingLabel);
		add(collapseButton);
		setCellHorizontalAlignment(headingLabel, HorizontalPanel.ALIGN_LEFT);
		setCellHorizontalAlignment(collapseButton, HorizontalPanel.ALIGN_RIGHT);
		setCellVerticalAlignment(headingLabel, HorizontalPanel.ALIGN_MIDDLE);
		
		headingLabel.setStylePrimaryName("gwt-HeadingPanelTitle");
	}
	
	public String getHeading(){
		return headingLabel.getText();
	}
	
	public void setHeading(String heading) {
		headingLabel.setText(heading);
	}
	
	public void setCollapsible(boolean collapsible){
		collapseButton.setVisible(collapsible);
	}

}

