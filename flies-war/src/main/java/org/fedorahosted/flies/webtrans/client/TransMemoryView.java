package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TransMemoryView extends FlowPanel implements TransMemoryPresenter.Display {

	private Button searchButton;
	private TextBox tmTextBox;
	
	private final FlowPanel resultsPanel;
	
	public TransMemoryView() {
		tmTextBox = new TextBox();
		searchButton = new Button("Search");
	
		add(tmTextBox);
		add(searchButton);
		resultsPanel = new FlowPanel();
		add(resultsPanel);
	}
	
	@Override
	public Button getSearchButton() {
		// TODO Auto-generated method stub
		return searchButton;
	}
	
	public TextBox getTmTextBox() {
		return tmTextBox;
	}

	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
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
	public void addResult(Widget widget) {
		resultsPanel.add(widget);
	}
	
	@Override
	public void clearResults() {
		resultsPanel.clear();
	}
}

