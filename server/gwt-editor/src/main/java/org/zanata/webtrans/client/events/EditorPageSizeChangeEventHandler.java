package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface EditorPageSizeChangeEventHandler extends EventHandler {
    void onPageSizeChange(EditorPageSizeChangeEvent event);
}
