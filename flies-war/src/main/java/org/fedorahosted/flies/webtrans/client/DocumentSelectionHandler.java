package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.EventHandler;

public interface DocumentSelectionHandler extends EventHandler {
	void onDocumentSelected(DocumentSelectionEvent event);
}
