package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface TransUnitSelectionHandler extends EventHandler {
    void onTransUnitSelected(TransUnitSelectionEvent event);
}
