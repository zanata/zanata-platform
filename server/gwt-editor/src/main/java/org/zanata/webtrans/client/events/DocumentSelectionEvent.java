package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.GwtEvent;

public class DocumentSelectionEvent extends GwtEvent<DocumentSelectionHandler>
        implements NavigationService.UpdateContextCommand {

    /**
     * Handler type.
     */
    private static final Type<DocumentSelectionHandler> TYPE = new Type<>();
    private final DocumentInfo document;

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<DocumentSelectionHandler> getType() {
        return TYPE;
    }

    public DocumentSelectionEvent(DocumentInfo documentInfo) {
        this.document = documentInfo;
    }

    public DocumentId getDocumentId() {
        return document.getId();
    }

    @Override
    protected void dispatch(DocumentSelectionHandler handler) {
        handler.onDocumentSelected(this);
    }

    @Override
    public GwtEvent.Type<DocumentSelectionHandler> getAssociatedType() {
        return getType();
    }

    @Override
    public GetTransUnitActionContext updateContext(
            GetTransUnitActionContext currentContext) {
        Preconditions.checkNotNull(currentContext,
                "current context can not be null");
        return currentContext.changeDocument(document);
    }
}
