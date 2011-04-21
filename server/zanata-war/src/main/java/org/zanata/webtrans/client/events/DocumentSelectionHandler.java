package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface DocumentSelectionHandler extends EventHandler
{
   void onDocumentSelected(DocumentSelectionEvent event);
}
