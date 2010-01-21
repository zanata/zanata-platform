package org.fedorahosted.flies.webtrans.editor.filter;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PhraseFilterView extends FlowPanel implements PhraseFilterPresenter.Display {
	
//	private final Label filterLabel;
	private final TextBox filterTextBox;
	
	public PhraseFilterView() {	
//		filterLabel = new Label("Filter by phrase");
//		filterLabel.setWordWrap(false);
		filterTextBox = new TextBox();
		filterTextBox.setWidth("100%");
//		add(filterLabel);
		add(filterTextBox);
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
