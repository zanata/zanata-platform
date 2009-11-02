package org.fedorahosted.flies.webtrans.editor.filter;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class FilterUnitView extends HorizontalPanel implements OperatorFilterPresenter.Display {
	
	private final Label filterLabel;
	private final TextBox filterTextBox;
	private final Button removeButton;
	
	public FilterUnitView() {	
		filterLabel = new Label("Filter: ");
		filterLabel.setWordWrap(false);
		filterTextBox = new TextBox();
		this.removeButton = new Button("X");
		add(filterLabel);
		add(filterTextBox);
		add(removeButton);
	}
	
	public FilterUnitView(String title, TextBox inputBox) {
		filterLabel = new Label("Filter: ");
		filterLabel.setWordWrap(false);
		filterTextBox = new TextBox();
		this.removeButton = new Button("X");
		add(filterLabel);
		add(filterTextBox);
		add(removeButton);
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

	@Override
	public Button getRemoveButton () {
		return removeButton;
	}
	
}
