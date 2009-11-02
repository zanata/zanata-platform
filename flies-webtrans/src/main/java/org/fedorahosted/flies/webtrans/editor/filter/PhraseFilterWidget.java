package org.fedorahosted.flies.webtrans.editor.filter;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PhraseFilterWidget extends HorizontalPanel implements PhraseFilterPresenter.Display {
	
	private final Label filterLabel;
	private final TextBox filterTextBox;
	private final Button deleteButton;
	
	public PhraseFilterWidget() {	
		filterLabel = new Label("Filter: ");
		filterLabel.setWordWrap(false);
		filterTextBox = new TextBox();
		this.deleteButton = new Button("X");
		add(filterLabel);
		add(filterTextBox);
		add(deleteButton);
	}
	
	public PhraseFilterWidget(String title, TextBox inputBox) {
		filterLabel = new Label("Filter: ");
		filterLabel.setWordWrap(false);
		filterTextBox = new TextBox();
		this.deleteButton = new Button("X");
		add(filterLabel);
		add(filterTextBox);
		add(deleteButton);
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
	public HasValue<String> getFilterText() {
		return filterTextBox;
	}

}
