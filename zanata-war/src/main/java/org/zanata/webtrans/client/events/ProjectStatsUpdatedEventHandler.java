package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface ProjectStatsUpdatedEventHandler extends EventHandler {
    void onProjectStatsUpdated(ProjectStatsUpdatedEvent event);
}
