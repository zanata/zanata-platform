package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;

public interface HasWorkspaceContextUpdateData
{
   boolean isProjectActive();

   ProjectType getProjectType();

   Map<ValidationId, ValidationInfo> getValidationInfoList();
}
