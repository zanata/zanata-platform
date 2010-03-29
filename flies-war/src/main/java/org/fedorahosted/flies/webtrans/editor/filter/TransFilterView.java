package org.fedorahosted.flies.webtrans.editor.filter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TransFilterView extends Composite implements TransFilterPresenter.Display {

	private FlowPanel bodyPanel;
	private Button findButton;
	
	public TransFilterView() {
		bodyPanel = new FlowPanel();
//		bodyPanel.setWidth("100%");

		Panel filterButtonBar = new FlowPanel();
		filterButtonBar.setStyleName("float-right-div");
		findButton = new Button("Find");
		filterButtonBar.add(findButton);
		
		bodyPanel.add(filterButtonBar);

//		RoundedContainerWithHeader container = new RoundedContainerWithHeader(new Label("Find Messages"), bodyPanel);
//		initWidget(container);
		
		DisclosurePanel container = new DisclosurePanel("Find Messages");
		container.setOpen(false);
		container.setAnimationEnabled(false);
		
		container.add(bodyPanel);
		initWidget(container);

		getElement().setId("TransFilterView");
	}
	
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
	public HasClickHandlers getFindButton() {
		return findButton;
	}
}
