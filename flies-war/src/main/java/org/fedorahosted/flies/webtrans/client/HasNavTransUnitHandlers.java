package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasNavTransUnitHandlers extends HasHandlers {
	HandlerRegistration addNavTransUnitHandler(NavTransUnitHandler handler);
}
