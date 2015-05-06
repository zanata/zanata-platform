package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;

public class WorkspaceContextUpdate implements SessionEventData,
        HasWorkspaceContextUpdateData {

    private static final long serialVersionUID = 1L;

    private boolean isProjectActive;
    private ProjectType projectType;
    private Map<ValidationId, State> validationStates;
    private String oldProjectSlug;
    private String newProjectSlug;
    private String oldIterationSlug;
    private String newIterationSlug;

    @SuppressWarnings("unused")
    private WorkspaceContextUpdate() {
    }

    public WorkspaceContextUpdate(boolean isProjectActive,
            ProjectType projectType, Map<ValidationId, State> validationStates) {
        this.isProjectActive = isProjectActive;
        this.projectType = projectType;
        this.validationStates = validationStates;
    }

    public WorkspaceContextUpdate projectSlugChanged(String oldSlug,
            String newSlug) {
        this.oldProjectSlug = oldSlug;
        this.newProjectSlug = newSlug;
        return this;
    }

    public WorkspaceContextUpdate iterationSlugChanged(String oldSlug,
            String newSlug) {
        this.oldIterationSlug = oldSlug;
        this.newIterationSlug = newSlug;
        return this;
    }

    @Override
    public boolean isProjectActive() {
        return isProjectActive;
    }

    @Override
    public ProjectType getProjectType() {
        return projectType;
    }

    @Override
    public Map<ValidationId, State> getValidationStates() {
        return validationStates;
    }

    @Override
    public String getOldProjectSlug() {
        return oldProjectSlug;
    }

    @Override
    public String getNewProjectSlug() {
        return newProjectSlug;
    }

    @Override
    public String getOldIterationSlug() {
        return oldIterationSlug;
    }

    @Override
    public String getNewIterationSlug() {
        return newIterationSlug;
    }
}
