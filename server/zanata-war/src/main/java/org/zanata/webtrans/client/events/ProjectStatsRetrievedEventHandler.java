package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface ProjectStatsRetrievedEventHandler extends EventHandler
{
   void onProjectStatsRetrieved(ProjectStatsRetrievedEvent event);
}
