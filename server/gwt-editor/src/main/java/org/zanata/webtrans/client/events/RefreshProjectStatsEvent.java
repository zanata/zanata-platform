package org.zanata.webtrans.client.events;

import java.util.List;

import org.zanata.webtrans.client.ui.DocumentNode;

import com.google.gwt.event.shared.GwtEvent;

public class RefreshProjectStatsEvent extends
        GwtEvent<ProjectStatsUpdatedEventHandler> {

    /**
     * Handler type.
     */
    private static final Type<ProjectStatsUpdatedEventHandler> TYPE = new Type<>();

    private List<DocumentNode> documentNodes;

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<ProjectStatsUpdatedEventHandler> getType() {
        return TYPE;
    }

    public RefreshProjectStatsEvent(List<DocumentNode> documentNodes) {
        this.documentNodes = documentNodes;
    }

    public List<DocumentNode> getDocumentNodes() {
        return documentNodes;
    }

    @Override
    protected void dispatch(ProjectStatsUpdatedEventHandler handler) {
        handler.onProjectStatsUpdated(this);
    }

    @Override
    public GwtEvent.Type<ProjectStatsUpdatedEventHandler> getAssociatedType() {
        return getType();
    }
}
