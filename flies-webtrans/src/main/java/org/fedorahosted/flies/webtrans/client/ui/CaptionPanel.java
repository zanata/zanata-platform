package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CaptionPanel extends DecoratorPanel {

	private VerticalPanel mainPanel;
	private HorizontalPanel headPanel;
	
	Label titleWidget;
	private Button collapseButton;
	private boolean collapsed;
	
	public CaptionPanel() {
		addStyleName("gwt-CaptionPanel");
		
		// init panel
		mainPanel = new VerticalPanel();
		mainPanel.setBorderWidth(0);
		mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		mainPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		mainPanel.setSpacing(0);
		mainPanel.setWidth("100%");
		setWidth("100%");
		
		headPanel = new HorizontalPanel();
		
		titleWidget = new Label();
		collapsed = true;
		collapseButton = new Button("-");
		collapseButton.setStylePrimaryName("gwt-CaptionPanel-Collapse");
		collapseButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setCollapsed(!collapsed);
			}
		});
		
		headPanel.addStyleName("gwt-CaptionPanel-Heading");
		headPanel.setWidth("100%");
		
		headPanel.add(titleWidget);
		headPanel.add(collapseButton);
		
		headPanel.setCellVerticalAlignment(titleWidget, HorizontalPanel.ALIGN_MIDDLE);
		headPanel.setCellHorizontalAlignment(collapseButton, HorizontalPanel.ALIGN_RIGHT);
		
		mainPanel.add(headPanel);
		add(mainPanel);
	}
	
	public void setBody(Widget widget) {
		if( mainPanel.getWidgetCount() == 2) {
			mainPanel.remove(1);
		}
		mainPanel.add(widget);
	}
	
	public void setTitle(String title) {
		titleWidget.setText(title);
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
		String collapseButtonIcon = collapsed ? "-" : "+";
		collapseButton.setText(collapseButtonIcon);
		mainPanel.getWidget(1).setVisible(collapsed);
	}
}
