package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.EventHandler;

public interface TransMemoryVisibilityHandler extends EventHandler{
	void onVisibilityChange(TransMemoryVisibilityEvent tabSelectionEvent);
}
