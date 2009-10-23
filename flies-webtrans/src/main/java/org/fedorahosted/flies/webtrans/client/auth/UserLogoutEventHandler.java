package org.fedorahosted.flies.webtrans.client.auth;

import com.google.gwt.event.shared.EventHandler;


public interface UserLogoutEventHandler extends EventHandler {
	
	void onUserLogout(UserLogoutEvent event);
	

}
