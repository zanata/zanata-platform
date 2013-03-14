package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;


public class WorkspaceContextUpdate implements SessionEventData, HasWorkspaceContextUpdateData
{

   private static final long serialVersionUID = 1L;

   private boolean isProjectActive;
   private ProjectType projectType;
   private Map<ValidationId, ValidationInfo> validationInfoList;

   @SuppressWarnings("unused")
   private WorkspaceContextUpdate()
   {
   }

   public WorkspaceContextUpdate(boolean isProjectActive, ProjectType projectType, Map<ValidationId, ValidationInfo> validationInfoList)
   {
      this.isProjectActive = isProjectActive;
      this.projectType = projectType;
      this.validationInfoList = validationInfoList;
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
   public Map<ValidationId, ValidationInfo> getValidationInfoList()
   {
      return validationInfoList;
   }

}
