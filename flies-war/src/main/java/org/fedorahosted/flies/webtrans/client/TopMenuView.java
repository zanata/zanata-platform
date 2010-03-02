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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TopMenuView extends Composite implements TopMenuPresenter.Display {

	interface TopMenuUiBinder extends UiBinder<Widget, TopMenuView> {
	}

	private static TopMenuUiBinder uiBinder = GWT.create(TopMenuUiBinder.class);

	@UiField
	Label userLabel, workspaceLabel, shortcutLabel;

	@UiField
	Button nextEntryButton, prevEntryButton, nextFuzzyButton, prevFuzzyButton,
			nextUntranslatedButton, prevUntranslatedButton;

	@UiField
	HorizontalPanel loginPanel;

	public TopMenuView() {
		initWidget(uiBinder.createAndBindUi(this));

		userLabel.setText("<Username>");
		workspaceLabel.setText("Workspace");

		// Text of navigations.
		String prevEntryText = "Prev Entry";
		String nextEntryText = "Next Entry";
		String prevFuzzyText = "Prev Fuzzy";
		String nextFuzzyText = "Next Fuzzy";
		String prevUntranslatedText = "Prev Untranslated";
		String nextUntranslatedText = "Next Untranslated";
		String prevEntryShortcut = NavigationConsts.PREV;
		String nextEntryShortcut = NavigationConsts.NEXT;
		String prevFuzzyShortcut = NavigationConsts.PREV_FUZZY;
		String nextFuzzyShortcut = NavigationConsts.NEXT_FUZZY;
		String prevUntranslatedShortcut = NavigationConsts.PREV_NEW;
		String nextUntranslatedShortcut = NavigationConsts.NEXT_NEW;
		
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

	// @Override
	// public HasClickHandlers getLogoutLink() {
	// return logoutLink;
	// }

	@Override
	public HasText getProjectName() {
		return workspaceLabel;
	}

	@Override
	public HasText getUsername() {
		return userLabel;
	}

	@Override
	public HasClickHandlers getPrevEntryButton() {
		// TODO Auto-generated method stub
		return prevEntryButton;
	}

	@Override
	public HasClickHandlers getNextEntryButton() {
		// TODO Auto-generated method stub
		return nextEntryButton;
	}

	@Override
	public HasClickHandlers getPrevFuzzyButton() {
		// TODO Auto-generated method stub
		return prevFuzzyButton;
	}

	@Override
	public HasClickHandlers getNextFuzzyButton() {
		// TODO Auto-generated method stub
		return nextFuzzyButton;
	}

	@Override
	public HasClickHandlers getPrevUntranslatedButton() {
		// TODO Auto-generated method stub
		return prevUntranslatedButton;
	}

	@Override
	public HasClickHandlers getNextUntranslatedButton() {
		// TODO Auto-generated method stub
		return nextUntranslatedButton;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public HasWidgets getWidgets() {
		return (HasWidgets)this.getWidget();
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
