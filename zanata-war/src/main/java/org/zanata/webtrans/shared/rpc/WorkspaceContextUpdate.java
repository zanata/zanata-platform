package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;


public class WorkspaceContextUpdate implements SessionEventData, HasWorkspaceContextUpdateData
{

   private static final long serialVersionUID = 1L;

   private boolean isProjectActive;
   private ProjectType projectType;
   private Map<ValidationId, State> validationStates;

   @SuppressWarnings("unused")
   private WorkspaceContextUpdate()
   {
   }

   public WorkspaceContextUpdate(boolean isProjectActive, ProjectType projectType, Map<ValidationId, State> validationStates)
   {
      this.isProjectActive = isProjectActive;
      this.projectType = projectType;
      this.validationStates = validationStates;
   }

   @Override
   public boolean isProjectActive()
   {
      return isProjectActive;
   }

   @Override
   public ProjectType getProjectType()
   {
      return projectType;
   }

   @Override
   public Map<ValidationId, State> getValidationStates()
   {
      return validationStates;
   }

}
