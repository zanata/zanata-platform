package org.zanata.webtrans.shared.rpc;

import org.zanata.common.ProjectType;

public interface HasWorkspaceContextUpdateData
{
   boolean isProjectActive();

   ProjectType getProjectType();
}
