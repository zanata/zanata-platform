package org.fedorahosted.flies.webtrans.client.auth;

import com.google.gwt.event.shared.EventHandler;

public interface UserLoginEventHandler extends EventHandler {
	
	void onUserLogin(UserLoginEvent event);

}
