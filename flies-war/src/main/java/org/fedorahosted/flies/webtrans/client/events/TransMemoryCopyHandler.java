package org.fedorahosted.flies.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface TransMemoryCopyHandler extends EventHandler {
	void onTransMemoryCopy(TransMemoryCopyEvent event);
}