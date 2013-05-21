package org.zanata.webtrans.client.events;


import org.zanata.common.CommonContainerTranslationStatistics;

import com.google.gwt.event.shared.GwtEvent;

public class ProjectStatsUpdatedEvent extends GwtEvent<ProjectStatsUpdatedEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<ProjectStatsUpdatedEventHandler> TYPE;
   private CommonContainerTranslationStatistics projectStats;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ProjectStatsUpdatedEventHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<ProjectStatsUpdatedEventHandler>());
   }

   public ProjectStatsUpdatedEvent(CommonContainerTranslationStatistics projectStats)
   {
      this.projectStats = projectStats;
   }

   public CommonContainerTranslationStatistics getProjectStats()
   {
      return projectStats;
   }

   @Override
   protected void dispatch(ProjectStatsUpdatedEventHandler handler)
   {
      handler.onProjectStatsRetrieved(this);
   }

   @Override
   public GwtEvent.Type<ProjectStatsUpdatedEventHandler> getAssociatedType()
   {
      return getType();
   }

}
