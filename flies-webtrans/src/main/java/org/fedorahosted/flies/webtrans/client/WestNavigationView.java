package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WestNavigationView extends FlowPanel implements
		WestNavigationPresenter.Display {

	private Button minimizeButton;
	private HorizontalPanel controllerPanel;
	
	public WestNavigationView() {
		getElement().setId("WestNavigationView");
		
		minimizeButton = new Button("-");
		controllerPanel = new HorizontalPanel();
		controllerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		controllerPanel.setWidth("100%");
		controllerPanel.add(minimizeButton);
		add(controllerPanel);
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
		return this;
	}
	
	@Override
	public Button getMinimizeButton() {
		return minimizeButton;
	}
	
	@Override
	public void clickedMinimizeButton() {
		if (minimizeButton.getText().equals("-")) {
			this.setWidth("25px");
			minimizeButton.setText("+");
		} else if (minimizeButton.getText().equals("+")) {
			this.setWidth("220px");
			minimizeButton.setText("-");
		}
	}
}
