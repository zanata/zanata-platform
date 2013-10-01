package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;

public interface HasWorkspaceContextUpdateData {
    boolean isProjectActive();

    ProjectType getProjectType();

    Map<ValidationId, State> getValidationStates();
}
