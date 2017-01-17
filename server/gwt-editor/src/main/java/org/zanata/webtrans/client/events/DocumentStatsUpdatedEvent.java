package org.zanata.webtrans.client.events;

import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.webtrans.shared.model.DocumentId;
import com.google.gwt.event.shared.GwtEvent;

public class DocumentStatsUpdatedEvent extends
        GwtEvent<DocumentStatsUpdatedEventHandler> {

    /**
     * Handler type.
     */
    private static final Type<DocumentStatsUpdatedEventHandler> TYPE = new Type<>();

    private DocumentId docId;
    private ContainerTranslationStatistics newStats;

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<DocumentStatsUpdatedEventHandler> getType() {
        return TYPE;
    }

    public DocumentStatsUpdatedEvent(DocumentId docId,
            ContainerTranslationStatistics newStats) {
        this.docId = docId;
        this.newStats = newStats;
    }

    public DocumentId getDocId() {
        return docId;
    }

    public ContainerTranslationStatistics getNewStats() {
        return newStats;
    }

    @Override
    protected void dispatch(DocumentStatsUpdatedEventHandler handler) {
        handler.onDocumentStatsUpdated(this);
    }

    @Override
    public GwtEvent.Type<DocumentStatsUpdatedEventHandler> getAssociatedType() {
        return getType();
    }
}
