package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.Person;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class UserListItem extends Composite {

	private static UserListItemUiBinder uiBinder = GWT
			.create(UserListItemUiBinder.class);

	interface UserListItemUiBinder extends UiBinder<Widget, UserListItem> {
	}

	@UiField
	SpanElement name;
	
	private Person user;

	public UserListItem() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public UserListItem(Person person) {
		this();
		setUser(person);
	}
	
	public void setUser(Person user) {
		this.user = user;
		refresh();
	}
	
	private void refresh() {
		name.setInnerText(user.getName());
	}
	
	


}
