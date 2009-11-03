package org.fedorahosted.flies.webtrans.editor.filter;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class OperatorFilterView extends VerticalPanel implements OperatorFilterPresenter.Display {
	
	private final VerticalPanel topPanel;
	private final HorizontalPanel bottomPanel;
	private final RadioButton andButton, orButton;
	private final Button addButton;
	
	public OperatorFilterView() {	
		topPanel = new VerticalPanel();
		bottomPanel = new HorizontalPanel();
		
		orButton = new RadioButton("andor", "OR");
		andButton = new RadioButton("andor", "AND");
		addButton = new Button("+");
		
		orButton.setValue(true);
		
		bottomPanel.add(orButton);
		bottomPanel.add(andButton);
		bottomPanel.add(addButton);
		
		add(topPanel);
		add(bottomPanel);
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
