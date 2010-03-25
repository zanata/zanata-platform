package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.editor.table.NavigationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitNavigationView extends Composite implements TransUnitNavigationPresenter.Display{

	private static TransUnitNavigationViewUiBinder uiBinder = GWT
			.create(TransUnitNavigationViewUiBinder.class);

	interface TransUnitNavigationViewUiBinder extends
			UiBinder<Widget, TransUnitNavigationView> {
	}

	@UiField
	Anchor nextEntryButton, prevEntryButton, nextFuzzyButton, prevFuzzyButton,
			nextUntranslatedButton, prevUntranslatedButton;

	private final NavigationMessages messages;
	
	@Inject
	public TransUnitNavigationView(final NavigationMessages messages) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.messages = messages;

		prevEntryButton.setText( messages.prevEntry() );
		nextEntryButton.setText( messages.nextEntry() );
		prevFuzzyButton.setText( messages.prevFuzzy() );
		nextFuzzyButton.setText( messages.nextFuzzy() );
		prevUntranslatedButton.setText( messages.prevUntranslated() );
		nextUntranslatedButton.setText( messages.nextUntranslated() );
		prevEntryButton.setTitle( messages.prevEntryShortcut() );
		nextEntryButton.setTitle( messages.nextEntryShortcut() );
		prevFuzzyButton.setTitle( messages.prevFuzzyShortcut() );
		nextFuzzyButton.setTitle( messages.nextFuzzyShortcut() );
		prevUntranslatedButton.setTitle( messages.prevUntranslatedShortcut() );
		nextUntranslatedButton.setTitle( messages.nextUntranslatedShortcut() );
	}

	@Override
	public HasClickHandlers getPrevEntryButton() {
		return prevEntryButton;
	}

	@Override
	public HasClickHandlers getNextEntryButton() {
		return nextEntryButton;
	}

	@Override
	public HasClickHandlers getPrevFuzzyButton() {
		return prevFuzzyButton;
	}

	@Override
	public HasClickHandlers getNextFuzzyButton() {
		return nextFuzzyButton;
	}

	@Override
	public HasClickHandlers getPrevUntranslatedButton() {
		return prevUntranslatedButton;
	}

	@Override
	public HasClickHandlers getNextUntranslatedButton() {
		return nextUntranslatedButton;
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
	
}
