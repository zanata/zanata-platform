package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface DocumentStatsUpdatedEventHandler extends EventHandler {
    void onDocumentStatsUpdated(DocumentStatsUpdatedEvent event);
}
