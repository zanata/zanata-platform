package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.editor.table.NavigationConsts;

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

public class TransUnitNavigationView extends Composite implements TransUnitNavigationPresenter.Display{

	private static final String prevEntryText = NavigationConsts.PREV_DESC;
	private static final String nextEntryText = NavigationConsts.NEXT_DESC;
	private static final String prevFuzzyText = NavigationConsts.PREV_FUZZY_DESC;
	private static final String nextFuzzyText = NavigationConsts.NEXT_FUZZY_DESC;
	private static final String prevUntranslatedText = NavigationConsts.PREV_NEW_DESC;
	private static final String nextUntranslatedText = NavigationConsts.NEXT_NEW_DESC;
	private static final String prevPageText = NavigationConsts.PREV_PAGE_DESC;
	private static final String nextPageText = NavigationConsts.NEXT_PAGE_DESC;
	private static final String firstPageText = NavigationConsts.FIRST_PAGE_DESC;
	private static final String lastPageText = NavigationConsts.LAST_PAGE_DESC;
	private static final String prevEntryShortcut = NavigationConsts.PREV_SHORTCUT;
	private static final String nextEntryShortcut = NavigationConsts.NEXT_SHORTCUT;
	private static final String prevFuzzyShortcut = NavigationConsts.PREV_FUZZY_SHORTCUT;
	private static final String nextFuzzyShortcut = NavigationConsts.NEXT_FUZZY_SHORTCUT;
	private static final String prevUntranslatedShortcut = NavigationConsts.PREV_NEW_SHORTCUT;
	private static final String nextUntranslatedShortcut = NavigationConsts.NEXT_NEW_SHORTCUT;

	private static TransUnitNavigationViewUiBinder uiBinder = GWT
			.create(TransUnitNavigationViewUiBinder.class);

	interface TransUnitNavigationViewUiBinder extends
			UiBinder<Widget, TransUnitNavigationView> {
	}

	@UiField
	Anchor nextEntryButton, prevEntryButton, nextFuzzyButton, prevFuzzyButton,
			nextUntranslatedButton, prevUntranslatedButton;

	@UiField
	Label shortcutLabel;

	
	public TransUnitNavigationView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		// texts remain set by code is good for I18N
		prevEntryButton.setText(prevEntryText);
		nextEntryButton.setText(nextEntryText);
		prevFuzzyButton.setText(prevFuzzyText);
		nextFuzzyButton.setText(nextFuzzyText);
		prevUntranslatedButton.setText(prevUntranslatedText);
		nextUntranslatedButton.setText(nextUntranslatedText);
		prevEntryButton.setTitle(prevEntryShortcut);
		nextEntryButton.setTitle(nextEntryShortcut);
		prevFuzzyButton.setTitle(prevFuzzyShortcut);
		nextFuzzyButton.setTitle(nextFuzzyShortcut);
		prevUntranslatedButton.setTitle(prevUntranslatedShortcut);
		nextUntranslatedButton.setTitle(nextUntranslatedShortcut);

		// Create list of shortcuts.
		// TODO need to convert NagivationConsts into enum?
		VerticalPanel shortcutListPanel = new VerticalPanel();
		shortcutListPanel.add(new Label("<Navigation Shortcuts>"));
		shortcutListPanel.add(new Label(prevEntryText + " - " + prevEntryShortcut));
		shortcutListPanel.add(new Label(nextEntryText + " - " + nextEntryShortcut));
		shortcutListPanel.add(new Label(prevFuzzyText + " - " + prevFuzzyShortcut));
		shortcutListPanel.add(new Label(nextFuzzyText + " - " + nextFuzzyShortcut));
		shortcutListPanel.add(new Label(prevUntranslatedText + " - " + prevUntranslatedShortcut));
		shortcutListPanel.add(new Label(nextUntranslatedText + " - " + nextUntranslatedShortcut));
		shortcutListPanel.add(new Label(prevPageText + " - " + NavigationConsts.PREV_PAGE_SHORTCUT));
		shortcutListPanel.add(new Label(nextPageText + " - " + NavigationConsts.NEXT_PAGE_SHORTCUT));
		shortcutListPanel.add(new Label(firstPageText + " - " + NavigationConsts.FIRST_PAGE_SHORTCUT));
		shortcutListPanel.add(new Label(lastPageText + " - " + NavigationConsts.LAST_PAGE_SHORTCUT));
		shortcutListPanel.add(new Label(NavigationConsts.EDIT_SAVE_DESC + " - " + NavigationConsts.EDIT_SAVE_SHORTCUT));
		shortcutListPanel.add(new Label(NavigationConsts.EDIT_CANCEL_DESC + " - " + NavigationConsts.EDIT_CANCEL_SHORTCUT));

		// Guide users about shortcut.
		shortcutLabel.setText("Show Shortcuts");
		final DecoratedPopupPanel popup = new DecoratedPopupPanel();
		popup.add(shortcutListPanel);
		shortcutLabel.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				popup.show();
				popup.setPopupPosition(
						getAbsoluteLeft() + getOffsetWidth() - popup.getOffsetWidth(),
						getAbsoluteTop() + getOffsetHeight());
			}
		});
		
		shortcutLabel.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				popup.hide();
			}
		});
		
		// logoutLink = new Hyperlink("Logout", "Logout");

		// transNavToolbarView = new TransNavToolbarView();
		// rightMenu.add(transNavToolbarView);

		// rightMenu.add(logoutLink);
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
