package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransFilterView extends CaptionPanel implements TransFilterPresenter.Display {

	private VerticalPanel vpanel;
	private Button filterEnableButton, filterDisableButton, addFilterButton;
	
	public TransFilterView() {
		vpanel = new VerticalPanel();
		vpanel.setWidth("100%");
		vpanel.setSpacing(10);
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		vpanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);

		filterEnableButton = new Button("Filter");
		filterDisableButton = new Button("Reset");
		addFilterButton = new Button("+");
		HorizontalPanel filterButtonBar = new HorizontalPanel();
		filterButtonBar.add(filterEnableButton);
		filterButtonBar.add(filterDisableButton);
		filterButtonBar.add(addFilterButton);
		vpanel.add(filterButtonBar);

		setTitle("Translation Unit Info");
		setBody(vpanel);
	}

	@Override
	public void addFilterUnitView(FilterUnitView filterUnitView) {
		vpanel.add(filterUnitView);
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
	public Button getDisableFilterButton() {
		return filterDisableButton;
	}
	
	@Override
	public Button getEnableFilterButton() {
		return filterEnableButton;
	}
	
	@Override
	public Button getAddFilterButton() {
		return addFilterButton;
	}
}
