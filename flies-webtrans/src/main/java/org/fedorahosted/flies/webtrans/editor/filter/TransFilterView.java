package org.fedorahosted.flies.webtrans.editor.filter;

import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransFilterView extends CaptionPanel implements TransFilterPresenter.Display {

	private final Button filterEnableButton, filterDisableButton;
	private final VerticalPanel vpanel = new VerticalPanel();

	private PhraseFilterWidget filter;
	
	public TransFilterView() {
		setTitle("Translation Unit Info");
		filterEnableButton = new Button("Filter");
		filterDisableButton = new Button("Reset");
		vpanel.setWidth("100%");
		vpanel.setSpacing(10);
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		vpanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		
		HorizontalPanel filterButtonBar = new HorizontalPanel();
		filterButtonBar.add(filterEnableButton);
		filterButtonBar.add(filterDisableButton);
				
		vpanel.add(filterButtonBar);
		
		setBody(vpanel);
	}

	@Override
	public void setFilter(PhraseFilterWidget filter) {
		this.filter = filter;
		vpanel.add(filter);
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
	public Button getFilterButton() {
		return filterEnableButton;
	}
}
