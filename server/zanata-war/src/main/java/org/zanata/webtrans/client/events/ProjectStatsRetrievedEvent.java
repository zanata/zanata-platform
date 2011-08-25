package org.zanata.webtrans.client.events;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.DocumentStatus;

import com.google.gwt.event.shared.GwtEvent;

public class ProjectStatsRetrievedEvent extends GwtEvent<ProjectStatsRetrievedEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<ProjectStatsRetrievedEventHandler> TYPE;
   private ArrayList<DocumentStatus> projectDocStats;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ProjectStatsRetrievedEventHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<ProjectStatsRetrievedEventHandler>());
   }

   public ProjectStatsRetrievedEvent(ArrayList<DocumentStatus> projectDocStats)
   {
      this.projectDocStats = projectDocStats;
   }

   public ArrayList<DocumentStatus> getProjectDocumentStats()
   {
      return projectDocStats;
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
