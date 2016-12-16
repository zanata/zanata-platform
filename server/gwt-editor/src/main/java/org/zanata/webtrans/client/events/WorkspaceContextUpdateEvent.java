package org.zanata.webtrans.client.events;

import java.util.Map;
import java.util.Objects;

import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;

import com.google.gwt.event.shared.GwtEvent;

public class WorkspaceContextUpdateEvent extends
        GwtEvent<WorkspaceContextUpdateEventHandler> {
    private final boolean isProjectActive;
    private final ProjectType projectType;
    private final Map<ValidationId, State> validationStates;
    private final String oldProjectSlug;
    private final String newProjectSlug;
    private final String oldIterationSlug;
    private final String newIterationSlug;


    public WorkspaceContextUpdateEvent(HasWorkspaceContextUpdateData data) {
        this.isProjectActive = data.isProjectActive();
        this.projectType = data.getProjectType();
        this.validationStates = data.getValidationStates();
        oldProjectSlug = data.getOldProjectSlug();
        newProjectSlug = data.getNewProjectSlug();
        oldIterationSlug = data.getOldIterationSlug();
        newIterationSlug = data.getNewIterationSlug();
    }

    /**
     * Handler type.
     */
    private static final Type<WorkspaceContextUpdateEventHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<WorkspaceContextUpdateEventHandler> getType() {
        return TYPE;
    }

    @Override
    public
            com.google.gwt.event.shared.GwtEvent.Type<WorkspaceContextUpdateEventHandler>
            getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(WorkspaceContextUpdateEventHandler handler) {
        handler.onWorkspaceContextUpdated(this);
    }

    public boolean isProjectActive() {
        return isProjectActive;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public Map<ValidationId, State> getValidationStates() {
        return validationStates;
    }

    public boolean hasSlugChanged() {
        return hasProjectSlugChanged() || hasIterationSlugChanged();
    }

    public boolean hasIterationSlugChanged() {
        return !Objects.equals(oldIterationSlug, newIterationSlug);
    }

    public boolean hasProjectSlugChanged() {
        return !Objects.equals(oldProjectSlug, newProjectSlug);
    }

    public String getNewProjectSlug() {
        return newProjectSlug;
    }

    public String getNewIterationSlug() {
        return newIterationSlug;
    }
}
