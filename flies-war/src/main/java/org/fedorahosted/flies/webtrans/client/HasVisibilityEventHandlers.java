package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasVisibilityEventHandlers extends HasHandlers {
	HandlerRegistration addVisibilityHandler(VisibilityHandler handler);
}
