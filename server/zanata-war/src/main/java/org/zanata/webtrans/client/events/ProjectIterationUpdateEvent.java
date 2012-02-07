package org.zanata.webtrans.client.events;

import org.zanata.common.EntityStatus;
import org.zanata.webtrans.shared.rpc.HasProjectIterationUpdateData;
import org.zanata.webtrans.shared.rpc.HasProjectUpdateData;

import com.google.gwt.event.shared.GwtEvent;

public class ProjectIterationUpdateEvent extends GwtEvent<ProjectIterationUpdateEventHandler>
{

   private final String projectSlug;
   private final EntityStatus projectStatus;

   private final String projectIterationSlug;
   private final EntityStatus projectIterationStatus;

   public ProjectIterationUpdateEvent(HasProjectIterationUpdateData data)
   {
      this.projectSlug = data.getProjectSlug();
      this.projectStatus = data.getProjectStatus();

      this.projectIterationSlug = data.getProjectIterationSlug();
      this.projectIterationStatus = data.getProjectIterationStatus();
   }

   /**
    * Handler type.
    */
   private static Type<ProjectIterationUpdateEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ProjectIterationUpdateEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<ProjectIterationUpdateEventHandler>();
      }
      return TYPE;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<ProjectIterationUpdateEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(ProjectIterationUpdateEventHandler handler)
   {
      handler.onProjectIterationUpdated(this);
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public EntityStatus getProjectStatus()
   {
      return projectStatus;
   }

   public String getProjectIterationSlug()
   {
      return projectIterationSlug;
   }

   public EntityStatus getProjectIterationStatus()
   {
      return projectIterationStatus;
   }

}
