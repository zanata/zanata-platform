package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.EventHandler;

public interface VisibilityHandler extends EventHandler{
	void onVisibilityChange(VisibilityEvent tabSelectionEvent);
}
