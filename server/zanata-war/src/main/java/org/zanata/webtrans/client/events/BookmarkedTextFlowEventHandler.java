package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface BookmarkedTextFlowEventHandler extends EventHandler {
    void onBookmarkableTextFlow(BookmarkedTextFlowEvent event);
}
