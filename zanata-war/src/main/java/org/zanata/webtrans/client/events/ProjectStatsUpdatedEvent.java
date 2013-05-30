package org.zanata.webtrans.client.events;


import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import com.google.gwt.event.shared.GwtEvent;

public class ProjectStatsUpdatedEvent extends GwtEvent<ProjectStatsUpdatedEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<ProjectStatsUpdatedEventHandler> TYPE;

   private ContainerTranslationStatistics newStats;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ProjectStatsUpdatedEventHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<ProjectStatsUpdatedEventHandler>());
   }

   public ProjectStatsUpdatedEvent(ContainerTranslationStatistics newStats)
   {
      this.newStats = newStats;
   }

   public ContainerTranslationStatistics getNewStats()
   {
      return newStats;
   }

   @Override
   protected void dispatch(ProjectStatsUpdatedEventHandler handler)
   {
      handler.onProjectStatsUpdated(this);
   }

   @Override
   public GwtEvent.Type<ProjectStatsUpdatedEventHandler> getAssociatedType()
   {
      return getType();
   }
}
