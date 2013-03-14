package org.zanata.webtrans.client.events;

import java.util.Map;

import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;

import com.google.gwt.event.shared.GwtEvent;

public class WorkspaceContextUpdateEvent extends GwtEvent<WorkspaceContextUpdateEventHandler>
{
   private final boolean isProjectActive;
   private final ProjectType projectType;
   private final Map<ValidationId, ValidationInfo> validationInfoList;

   public WorkspaceContextUpdateEvent(HasWorkspaceContextUpdateData data)
   {
      this.isProjectActive = data.isProjectActive();
      this.projectType = data.getProjectType();
      this.validationInfoList = data.getValidationInfoList();
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

   public ProjectType getProjectType()
   {
      return projectType;
   }

   public Map<ValidationId, ValidationInfo> getValidationInfoList()
   {
      return validationInfoList;
   }
}
