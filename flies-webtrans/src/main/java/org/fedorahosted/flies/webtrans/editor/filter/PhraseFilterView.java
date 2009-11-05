package org.fedorahosted.flies.webtrans.editor.filter;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PhraseFilterView extends FlowPanel implements PhraseFilterPresenter.Display {
	
	private final Label filterLabel;
	private final TextBox filterTextBox;

	//temp removeButton, delete this when proper coding is done :)
	private final Button removeButton;
	
	public PhraseFilterView() {	
		filterLabel = new Label("Filter by phrase");
		filterLabel.setWordWrap(false);
		filterTextBox = new TextBox();
		filterTextBox.setWidth("75px");
		add(filterLabel);
		add(filterTextBox);
		
		//temp removeButton, delete this when proper coding is done :)
		removeButton = new Button("-");
		add(removeButton);
	}
	
	//temp removeButton, delete this when proper coding is done :)
	@Override
	public Button getRemoveButton() {
		return removeButton;
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
