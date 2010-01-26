package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TopMenuView extends HorizontalPanel implements TopMenuPresenter.Display{

	private final Label userLabel;
	private final Label workspaceLabel;
	//private final Hyperlink logoutLink;
	private TransNavToolbarView transNavToolbarView;

	private final HorizontalPanel rightMenu;

	public TopMenuView() {
		setHeight("20px");
		setWidth("100%");

		rightMenu = new HorizontalPanel();
		rightMenu.setSpacing(5);
		userLabel = new Label("<Username>");
		workspaceLabel = new Label("Workspace");

		//logoutLink = new Hyperlink("Logout", "Logout");
		
		//transNavToolbarView = new TransNavToolbarView();
		//rightMenu.add(transNavToolbarView);

		rightMenu.add(userLabel);
		//rightMenu.add(logoutLink);
		
		add(workspaceLabel);
		setCellHorizontalAlignment(workspaceLabel, HorizontalPanel.ALIGN_LEFT);
		
		add(rightMenu);
		setCellHorizontalAlignment(rightMenu, HorizontalPanel.ALIGN_RIGHT);
		
	}

	//@Override
	//public HasClickHandlers getLogoutLink() {
	//	return logoutLink;
	//}

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

	@Override
	public HasWidgets getWidgets() {
		// TODO Auto-generated method stub
		return rightMenu;
	}

}
