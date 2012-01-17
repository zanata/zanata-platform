package org.zanata.webtrans.client.events;


import org.zanata.common.TranslationStats;

import com.google.gwt.event.shared.GwtEvent;

public class ProjectStatsUpdatedEvent extends GwtEvent<ProjectStatsUpdatedEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<ProjectStatsUpdatedEventHandler> TYPE;
   private TranslationStats projectStats;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ProjectStatsUpdatedEventHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<ProjectStatsUpdatedEventHandler>());
   }

   public ProjectStatsUpdatedEvent(TranslationStats projectStats)
   {
      this.projectStats = projectStats;
   }

   public TranslationStats getProjectStats()
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
