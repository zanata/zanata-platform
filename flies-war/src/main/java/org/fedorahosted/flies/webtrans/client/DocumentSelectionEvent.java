package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.DocumentId;

import com.google.gwt.event.shared.GwtEvent;

public class DocumentSelectionEvent extends GwtEvent<DocumentSelectionHandler> {
	
	/**
	 * Handler type.
	 */
	private static Type<DocumentSelectionHandler> TYPE;
	private final DocumentId documentId;
	
	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<DocumentSelectionHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<DocumentSelectionHandler>());
	}

	public DocumentSelectionEvent(DocumentId id) {
		this.documentId = id;
	}
	
	public DocumentId getDocumentId() {
		return documentId;
	}
	
	@Override
	protected void dispatch(DocumentSelectionHandler handler) {
		handler.onDocumentSelected(this);
	}

	@Override
	public GwtEvent.Type<DocumentSelectionHandler> getAssociatedType() {
		return getType();
	}

}
