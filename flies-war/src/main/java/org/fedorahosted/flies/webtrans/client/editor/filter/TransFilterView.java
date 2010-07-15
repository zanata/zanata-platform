package org.fedorahosted.flies.webtrans.client.editor.filter;

import org.fedorahosted.flies.webtrans.client.Resources;
import org.fedorahosted.flies.webtrans.client.editor.filter.TransFilterMessages;
import org.fedorahosted.flies.webtrans.client.ui.ClearableTextBox;
import org.fedorahosted.flies.webtrans.client.ui.CollapsePanel;
import org.fedorahosted.flies.webtrans.client.ui.UiMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransFilterView extends Composite implements TransFilterPresenter.Display {
	
	private static TransFilterViewUiBinder uiBinder = GWT
	.create(TransFilterViewUiBinder.class);

	interface TransFilterViewUiBinder extends UiBinder<Widget, TransFilterView> {
	}

	@UiField(provided=true)
	CollapsePanel collapsePanel;
	
	@UiField(provided=true)
	ClearableTextBox filterTextBox;
	
	private final TransFilterMessages messages;
	
	@Inject
	public TransFilterView(final Resources resources, final TransFilterMessages messages, final UiMessages uiMessages) {
		this.messages = messages;
		this.collapsePanel = new CollapsePanel(resources);
		this.filterTextBox = new ClearableTextBox(resources, uiMessages );
		filterTextBox.setEmptyText( messages.findSourceOrTargetString() );
		initWidget( uiBinder.createAndBindUi(this) );
		collapsePanel.setHeading( messages.transUnitSearchesHeading() );
		getElement().setId("TransFilterView");
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}
	
	@Override
	public TextBox getFilterText() {
		return filterTextBox.getTextBox();
	}
	
}
