package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.EventHandler;

public interface NotificationEventHandler extends EventHandler{

	void onNotification(NotificationEvent event);
	
}
