package org.fedorahosted.flies.webtrans.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WestNavigationView extends Composite implements
		WestNavigationPresenter.Display {

	final VerticalPanel panel;
	
	public WestNavigationView() {
		Log.info("setting up LeftNavigationView");
		panel = new VerticalPanel();
		panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		panel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		panel.setSpacing(5);
		panel.setStylePrimaryName("LeftContentNavBar");
		initWidget(panel);
		panel.setWidth("220px");
	}

	@Override
	public void setHeight(String height) {
		panel.setHeight(height);
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

}
