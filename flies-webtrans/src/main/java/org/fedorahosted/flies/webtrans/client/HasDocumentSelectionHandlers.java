package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasDocumentSelectionHandlers extends HasHandlers {
	HandlerRegistration addDocumentSelectionHandler(DocumentSelectionHandler handler);
}
