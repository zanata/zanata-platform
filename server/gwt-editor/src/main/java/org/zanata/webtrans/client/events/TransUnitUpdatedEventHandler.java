package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface TransUnitUpdatedEventHandler extends EventHandler {

    void onTransUnitUpdated(TransUnitUpdatedEvent event);

}
