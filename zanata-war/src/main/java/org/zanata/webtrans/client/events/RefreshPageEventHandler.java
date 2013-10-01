package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface RefreshPageEventHandler extends EventHandler {
    void onRefreshPage(RefreshPageEvent event);
}
