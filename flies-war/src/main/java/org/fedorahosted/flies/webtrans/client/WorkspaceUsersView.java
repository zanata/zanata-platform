package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.Person;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WorkspaceUsersView extends Composite implements
		WorkspaceUsersPresenter.Display {

	private static WorkspaceUsersViewUiBinder uiBinder = GWT
			.create(WorkspaceUsersViewUiBinder.class);

	interface WorkspaceUsersViewUiBinder extends UiBinder<LayoutPanel, WorkspaceUsersView> {
	}

	@UiField
	FlowPanel userListPanel;
	
	@UiField
	Label headerLabel;
	
	private final WebTransMessages messages;
	
	@Inject
	public WorkspaceUsersView(WebTransMessages messages) {
		this.messages = messages;
		initWidget(uiBinder.createAndBindUi(this));
		headerLabel.setText(messages.nUsersOnline(0));
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
	public void updateUserList(ArrayList<Person> userList) {
		int existingCount = userListPanel.getWidgetCount();
		UserListItem item = null;
		for(int i=0;i<userList.size();i++) {
			if(existingCount > i) {
				item = (UserListItem) userListPanel.getWidget(i);
				item.setUser(userList.get(i));
			}
			else{
				item = new UserListItem(userList.get(i));
				userListPanel.add(item);
			}
		}
		for(int i= userList.size();i<existingCount;i++){
			userListPanel.remove(i-1);
		}
		
		headerLabel.setText(messages.nUsersOnline(userList.size()));
		
	}

	@Override
	public HasMouseOverHandlers getCollapsedWidget() {
		return headerLabel;
	}
}
