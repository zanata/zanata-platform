package org.fedorahosted.flies.webtrans.editor.filter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TransFilterView extends Composite implements TransFilterPresenter.Display {
	private static final boolean INITIALLY_OPEN = false;
	private static final boolean RENDER_REPLACE = false;

	private FlowPanel bodyPanel;
	private Button findButton = new Button("Find");
	private TextBox replaceText = new TextBox();
	private Button replaceButton;
	private Button replaceAllButton;
	private Widget filterUnitPanel;
	
	public TransFilterView() {
		bodyPanel = new FlowPanel();
//		bodyPanel.setWidth("100%");

		Panel filterButtonBar = new FlowPanel();
		filterButtonBar.setStyleName("float-right-div");
		filterButtonBar.add(findButton);
		bodyPanel.add(filterButtonBar);

		
		if (RENDER_REPLACE) {
			bodyPanel.add(replaceText);
			
			Panel replaceButtonBar = new FlowPanel();
			replaceButtonBar.setStyleName("float-right-div");
			replaceButton = new Button("Replace");
			replaceButtonBar.add(replaceButton);
			replaceAllButton = new Button("Replace All");
			replaceButtonBar.add(replaceAllButton);
			bodyPanel.add(replaceButtonBar);
		}
		
//		RoundedContainerWithHeader container = new RoundedContainerWithHeader(new Label("Find Messages"), bodyPanel);
//		initWidget(container);
		
		DisclosurePanel container = new DisclosurePanel("Find Messages");
		container.setOpen(INITIALLY_OPEN);
		container.setAnimationEnabled(false);
		
		container.add(bodyPanel);
		initWidget(container);

		getElement().setId("TransFilterView");
	}
	
	@Override
	public void setFilterUnitPanel (Widget widget) {
		if (filterUnitPanel != null) {
			bodyPanel.remove(filterUnitPanel);
			filterUnitPanel = null;
		} else {
			bodyPanel.insert(widget,0);
			filterUnitPanel = widget;
		}
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
