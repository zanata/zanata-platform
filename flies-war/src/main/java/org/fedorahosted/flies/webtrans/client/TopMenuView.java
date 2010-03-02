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

		// texts remain set by code is good for I18N
		prevEntryButton.setText("Prev Entry");
		nextEntryButton.setText("Next Entry");
		prevFuzzyButton.setText("Prev Fuzzy");
		nextFuzzyButton.setText("Next Fuzzy");
		prevUntranslatedButton.setText("Prev Untranslated");
		nextUntranslatedButton.setText("Next Untranslated");
		prevEntryButton.setTitle(NavigationConsts.PREV);
		nextEntryButton.setTitle(NavigationConsts.NEXT);
		prevFuzzyButton.setTitle(NavigationConsts.PREV_FUZZY);
		nextFuzzyButton.setTitle(NavigationConsts.NEXT_FUZZY);
		prevUntranslatedButton.setTitle(NavigationConsts.PREV_NEW);
		nextUntranslatedButton.setTitle(NavigationConsts.NEXT_NEW);

		userLabel.setText("<Username>");
		workspaceLabel.setText("Workspace");

		// Create list of shortcuts.
		// TODO need to convert NagivationConsts into enum?
		VerticalPanel shortcutListPanel = new VerticalPanel();
		shortcutListPanel.add(new Label("<Navigation Shortcuts>"));
		shortcutListPanel.add(new Label("Previous Entry - " + NavigationConsts.PREV));
		shortcutListPanel.add(new Label("Next Entry - " + NavigationConsts.NEXT));
		shortcutListPanel.add(new Label("Previous Fuzzy - " + NavigationConsts.PREV_FUZZY));
		shortcutListPanel.add(new Label("Next Fuzzy - " + NavigationConsts.NEXT_FUZZY));
		shortcutListPanel.add(new Label("Previous Untranslated - " + NavigationConsts.PREV_NEW));
		shortcutListPanel.add(new Label("Next Untranslated - " + NavigationConsts.PREV_NEW));

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
