package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface InsertStringInEditorHandler extends EventHandler {
    void onInsertString(InsertStringInEditorEvent event);
}
