package org.fedorahosted.flies.webtrans.editor;

import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GlossaryView extends VerticalPanel implements GlossaryPresenter.Display {

	private Button searchButton;
	private Label glossaryLabel;
	private TextBox glossaryTextBox;
	
	private final FlowPanel resultsPanel;
	
	public GlossaryView() {
		glossaryLabel = new Label("Input the term");
		glossaryTextBox = new TextBox();
		searchButton = new Button("Search");
	
		add(glossaryLabel);
		add(glossaryTextBox);
		add(searchButton);
		resultsPanel = new FlowPanel();
		add(resultsPanel);

	}
	
	@Override
	public Button getSearchButton() {
		// TODO Auto-generated method stub
		return searchButton;
	}
	
	public TextBox getGlossaryTextBox() {
		return glossaryTextBox;
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
