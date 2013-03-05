package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.ValidationInfo;

public interface HasWorkspaceContextUpdateData
{
   boolean isProjectActive();

   ProjectType getProjectType();

   List<ValidationInfo> getValidationInfoList();
}
