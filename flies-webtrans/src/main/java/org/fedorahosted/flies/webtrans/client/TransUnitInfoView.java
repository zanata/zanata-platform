package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransUnitInfoView extends CaptionPanel implements TransUnitInfoPresenter.Display {

	private static TextBox filter1Label, filter2Label;
	private static RadioButton filterAndTypeRadio, filterOrTypeRadio;
	private static Button filterEnableButton, filterDisableButton;
	
	public TransUnitInfoView() {
		setTitle("Translation Unit Info");
		setBody(getStatusPanel());
	}

	public static Widget getStatusPanel() {
		VerticalPanel vpanel = new VerticalPanel();
		vpanel.setWidth("100%");
		vpanel.setHeight("100px");
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		vpanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		
		filter1Label = new TextBox();
		filter2Label = new TextBox();
		
		HorizontalPanel filterTypeRadioBar = new HorizontalPanel();
		filterAndTypeRadio = new RadioButton(null);
		filterAndTypeRadio.setText("And");
		filterOrTypeRadio = new RadioButton(null);
		filterOrTypeRadio.setText("Or");
		filterTypeRadioBar.add(filterAndTypeRadio);
		filterTypeRadioBar.add(filterOrTypeRadio);
		
		HorizontalPanel filterButtonBar = new HorizontalPanel();
		filterEnableButton = new Button("Filter");
		filterDisableButton = new Button("Reset");
		filterButtonBar.add(filterEnableButton);
		filterButtonBar.add(filterDisableButton);
				
		//ProgressBar bar = new ProgressBar(0.0, 2000.0,0.0);
		//bar.setProgress(1500.0);
		//bar.setTitle("Complete");
		filterAndTypeRadio.setValue(true);
		vpanel.add(filter1Label);
		vpanel.add(filterTypeRadioBar);
		vpanel.add(filter2Label);
		vpanel.add(filterButtonBar);
		//vpanel.add(bar);
		return vpanel;
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
}
