package org.fedorahosted.flies.webtrans.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WestNavigationView extends SimplePanel implements
		WestNavigationPresenter.Display {

	private final VerticalPanel panel;
	private final Button hideButton;
	private boolean hidden;
	
	public WestNavigationView() {
		Log.info("setting up LeftNavigationView");
		
		panel = new VerticalPanel();
		panel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		panel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		panel.setWidth("220px");

		hideButton = new Button("-");

		setHeight("100%");
		
		showThis();

//		setStylePrimaryName("gwt-CollapsiblePanel");
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
		return panel;
	}
	
	@Override
	public void showThis() {
		hidden = false;
		hideButton.setText("-");
		setWidth(panel.getOffsetWidth() + "px");
		remove(hideButton);
		panel.insert(hideButton, 0);
		add(panel);
	}
	
	@Override
	public void hideThis() {
		hidden = true;
		hideButton.setText("+");
		setWidth(hideButton.getOffsetWidth() + "px");
		remove(panel);
		add(hideButton);
	}
	
	@Override
	public Button getHideButton() {
		return hideButton;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}
}
