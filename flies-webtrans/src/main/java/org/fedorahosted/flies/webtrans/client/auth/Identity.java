package org.fedorahosted.flies.webtrans.client.auth;

import org.fedorahosted.flies.gwt.auth.Permission;
import org.fedorahosted.flies.gwt.auth.Role;
import org.fedorahosted.flies.gwt.model.Person;

public interface Identity {
	
	public boolean isLoggedIn();

	boolean hasRole(Role role);

	boolean hasPermission(Permission permission);

	Person getPerson();

	String getSessionId();

	void invalidate();

	void login(String username, String password, LoginResult callback);
}
