package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.Person;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class TopMenuView extends HorizontalPanel implements TopMenuPresenter.Display{

	private final Label userLabel;
	private final Label workspaceLabel;
	private final Label notLoggedInLabel;
	private final Hyperlink logoutLink;

	private final HorizontalPanel rightMenu;

	public TopMenuView() {
		setHeight("20px");
		setWidth("100%");

		rightMenu = new HorizontalPanel();
		rightMenu.setSpacing(5);
		userLabel = new Label("<Username>");
		workspaceLabel = new Label("Workspace");
		userLabel.setVisible(false);

		notLoggedInLabel = new Label("Not Logged In");

		logoutLink = new Hyperlink("Logout", "Logout");

		rightMenu.add(notLoggedInLabel);
		rightMenu.add(userLabel);
		rightMenu.add(logoutLink);

		add(workspaceLabel);
		setCellHorizontalAlignment(workspaceLabel, HorizontalPanel.ALIGN_LEFT);
		
		add(rightMenu);
		setCellHorizontalAlignment(rightMenu, HorizontalPanel.ALIGN_RIGHT);
	}

	public void setUser(Person person) {
		userLabel.setText(person.getName());
		notLoggedInLabel.setVisible(false);
		userLabel.setVisible(true);
	}

	public void clearUser() {
		userLabel.setVisible(false);
		notLoggedInLabel.setVisible(true);
	}

	@Override
	public HasClickHandlers getLogoutLink() {
		return logoutLink;
	}

	@Override
	public HasText getProjectName() {
		return workspaceLabel;
	}

	@Override
	public HasText getUsername() {
		return userLabel;
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
