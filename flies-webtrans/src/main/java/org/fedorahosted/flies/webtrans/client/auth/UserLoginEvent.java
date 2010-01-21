package org.fedorahosted.flies.webtrans.client.auth;

import org.fedorahosted.flies.gwt.model.Person;

import com.google.gwt.event.shared.GwtEvent;

public class UserLoginEvent extends GwtEvent<UserLoginEventHandler> {

	private final Person person;
	
	public UserLoginEvent(Person person) {
		this.person = person;
	}
	
	/**
	 * Handler type.
	 */
	private static Type<UserLoginEventHandler> TYPE;

	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<UserLoginEventHandler> getType() {
		if (TYPE == null) {
			TYPE = new Type<UserLoginEventHandler>();
		}
		return TYPE;
	}
	
	@Override
	protected void dispatch(UserLoginEventHandler handler) {
		handler.onUserLogin(this);
	}

	@Override
	public Type<UserLoginEventHandler> getAssociatedType() {
		return TYPE;
	}

	public Person getPerson() {
		return person;
	}
	
}
