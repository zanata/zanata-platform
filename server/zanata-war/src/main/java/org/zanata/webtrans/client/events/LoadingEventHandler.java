package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface LoadingEventHandler extends EventHandler {
    void onLoading(LoadingEvent event);
}
