package org.zanata.webtrans.client.events;

import org.zanata.common.EntityStatus;
import org.zanata.webtrans.shared.rpc.HasProjectUpdateData;

import com.google.gwt.event.shared.GwtEvent;

public class ProjectUpdateEvent extends GwtEvent<ProjectUpdateEventHandler>
{

   private final String projectSlug;
   private final EntityStatus status;

   public ProjectUpdateEvent(HasProjectUpdateData data)
   {
      this.projectSlug = data.getProjectSlug();
      this.status = data.getProjectStatus();
   }

   /**
    * Handler type.
    */
   private static Type<ProjectUpdateEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ProjectUpdateEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<ProjectUpdateEventHandler>();
      }
      return TYPE;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<ProjectUpdateEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(ProjectUpdateEventHandler handler)
   {
      handler.onProjectUpdated(this);
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public EntityStatus getProjectStatus()
   {
      return status;
   }

}
