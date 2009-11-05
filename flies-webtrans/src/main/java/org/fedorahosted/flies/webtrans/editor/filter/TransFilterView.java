package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.webtrans.client.RoundedContainerWithHeader;
import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransFilterView extends Composite implements TransFilterPresenter.Display {

	private FlowPanel bodyPanel;
	private Button applyButton, disableButton;
	
	public TransFilterView() {
		bodyPanel = new FlowPanel();
		bodyPanel.setWidth("100%");

		FlowPanel filterButtonBar = new FlowPanel();
		filterButtonBar.setStyleName("float-right-div");
		applyButton = new Button("Apply");
		applyButton.addClickHandler(clickHandler);
		disableButton = new Button("Disable");
		disableButton.addClickHandler(clickHandler);
		disableButton.setVisible(false);
		filterButtonBar.add(applyButton);
		filterButtonBar.add(disableButton);
		
		bodyPanel.add(filterButtonBar);

		RoundedContainerWithHeader container = new RoundedContainerWithHeader(new Label("Filtering"), bodyPanel);
		initWidget(container);
		setWidth("100%");	
	}

	private final ClickHandler clickHandler = new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			boolean wasApply = event.getSource() == applyButton;
			applyButton.setVisible(!wasApply);
			disableButton.setVisible(wasApply);
		}
	};
	
	@Override
	public void setFilterUnitPanel (Widget widget) {
		if (bodyPanel.getWidgetCount() == 2)
			bodyPanel.remove(0);
		else
			bodyPanel.insert(widget,0);
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
	public Button getDisableButton() {
		return disableButton;
	}
	
	@Override
	public Button getApplyButton() {
		return applyButton;
	}
}
