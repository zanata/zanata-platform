package org.zanata.webtrans.shared.rpc;

import org.zanata.common.ProjectType;


public class WorkspaceContextUpdate implements SessionEventData, HasWorkspaceContextUpdateData
{

   private static final long serialVersionUID = 1L;

   private boolean isProjectActive;
   private ProjectType projectType;

   @SuppressWarnings("unused")
   private WorkspaceContextUpdate()
   {
   }

   public WorkspaceContextUpdate(boolean isProjectActive, ProjectType projectType)
   {
      this.isProjectActive = isProjectActive;
      this.projectType = projectType;
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

}
