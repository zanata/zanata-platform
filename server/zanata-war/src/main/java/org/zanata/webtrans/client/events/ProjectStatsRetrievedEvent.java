package org.zanata.webtrans.client.events;


import org.zanata.common.TranslationStats;

import com.google.gwt.event.shared.GwtEvent;

public class ProjectStatsRetrievedEvent extends GwtEvent<ProjectStatsRetrievedEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<ProjectStatsRetrievedEventHandler> TYPE;
   private TranslationStats projectStats;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ProjectStatsRetrievedEventHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<ProjectStatsRetrievedEventHandler>());
   }

   public ProjectStatsRetrievedEvent(TranslationStats projectStats)
   {
      this.projectStats = projectStats;
   }

   public TranslationStats getProjectStats()
   {
      return projectStats;
   }

   @Override
   protected void dispatch(ProjectStatsRetrievedEventHandler handler)
   {
      handler.onProjectStatsRetrieved(this);
   }

   @Override
   public GwtEvent.Type<ProjectStatsRetrievedEventHandler> getAssociatedType()
   {
      return getType();
   }

}
