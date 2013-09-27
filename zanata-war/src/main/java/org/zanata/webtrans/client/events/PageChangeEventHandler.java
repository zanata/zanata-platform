package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface PageChangeEventHandler extends EventHandler {
    void onPageChange(PageChangeEvent event);
}
