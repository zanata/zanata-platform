package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WestNavigationView extends FlowPanel implements
		WestNavigationPresenter.Display {

	private static final String MAX_TEXT = "->";
	private static final String MAX_RESTORE_TEXT = "[->]";
	private static final String MIN_TEXT = "<-";
	private static final String MIN_RESTORE_TEXT = "[<-]";
	private static final String NORMAL_WIDTH = "220px";
	private static final String MAX_WIDTH = "600px";
	private static final String MIN_WIDTH = "25px";
	private HorizontalPanel controllerPanel;
	private Button minimizeButton;
	private boolean minimized;
	private Button maximizeButton;
	private boolean maximized;
	private FlowPanel widgetPanel = new FlowPanel();
	
	public WestNavigationView() {
		getElement().setId("WestNavigationView");
		
		minimizeButton = new Button(MIN_TEXT);
		maximizeButton = new Button(MAX_TEXT);
		controllerPanel = new HorizontalPanel();
		controllerPanel.setWidth("100%");
		controllerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		controllerPanel.add(minimizeButton);
		controllerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		controllerPanel.add(maximizeButton);
		add(controllerPanel);
		add(widgetPanel);
		minimizeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clickedMinimizeButton();
			}
		});
		maximizeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clickedMaximizeButton();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopProcessing() {
		// TODO Auto-generated method stub
	}

	@Override
	public HasWidgets getWidgets() {
		return widgetPanel;
	}
	
	private void clickedMinimizeButton() {
		if (!minimized) {
			widgetPanel.setVisible(false);
			maximizeButton.setVisible(false);
			this.setWidth(MIN_WIDTH);
			minimizeButton.setText(MIN_RESTORE_TEXT);
		} else {
			widgetPanel.setVisible(true);
			maximizeButton.setVisible(true);
			this.setWidth(NORMAL_WIDTH);
			minimizeButton.setText(MIN_TEXT);
		}
		minimized = !minimized;
		maximized = false;
		maximizeButton.setText(MAX_TEXT);
	}
	private void clickedMaximizeButton() {
		if (!maximized) {
			this.setAllWidths(MAX_WIDTH);
			resizeChildWidgets();
			maximizeButton.setText(MAX_RESTORE_TEXT);
		} else {
			this.setAllWidths(NORMAL_WIDTH);
			resizeChildWidgets();
			maximizeButton.setText(MAX_TEXT);
		}
		maximized = !maximized;
	}
	
	private void resizeChildWidgets() {
		// ugly workaround to force GWT to resize the sub-widgets
		widgetPanel.setVisible(false);
		widgetPanel.setVisible(true);
	}
	
	public void setAllWidths(String width) {
		super.setWidth(width);
		for (Widget w : getWidgets()) {
			w.setWidth(width);
		}
	}
}
