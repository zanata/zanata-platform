package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;

import com.google.gwt.event.shared.GwtEvent;

public class WorkspaceContextUpdateEvent extends GwtEvent<WorkspaceContextUpdateEventHandler>
{
   private final boolean isProjectActive;

   public WorkspaceContextUpdateEvent(HasWorkspaceContextUpdateData data)
   {
      this.isProjectActive = data.isProjectActive();
   }

   /**
    * Handler type.
    */
   private static Type<WorkspaceContextUpdateEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<WorkspaceContextUpdateEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<WorkspaceContextUpdateEventHandler>();
      }
      return TYPE;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<WorkspaceContextUpdateEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(WorkspaceContextUpdateEventHandler handler)
   {
      handler.onWorkspaceContextUpdated(this);
   }

   public boolean isProjectActive()
   {
      return isProjectActive;
   }

}
