package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransFilterView extends CaptionPanel implements TransFilterPresenter.Display {

	private VerticalPanel vpanel;
	private Button filterEnableButton, filterDisableButton;
	
	public TransFilterView() {
		vpanel = new VerticalPanel();
		vpanel.setWidth("100%");
		vpanel.setSpacing(10);
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		vpanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);

		filterEnableButton = new Button("Filter");
		filterDisableButton = new Button("Reset");
		
		HorizontalPanel filterButtonBar = new HorizontalPanel();
		filterButtonBar.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		filterButtonBar.add(filterEnableButton);
		filterButtonBar.add(filterDisableButton);
		
		vpanel.add(filterButtonBar);

		setTitle("Filter");
		setBody(vpanel);
	}

	@Override
	public void setFilterUnitPanel (Widget widget) {
		if (vpanel.getWidgetCount() == 2)
			vpanel.remove(1);
		else
			vpanel.add(widget);
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
}
