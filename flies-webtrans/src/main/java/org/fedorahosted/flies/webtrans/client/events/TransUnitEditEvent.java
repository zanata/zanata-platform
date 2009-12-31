package org.fedorahosted.flies.webtrans.client.events;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.HasTransUnitEditData;

public class TransUnitEditEvent  extends SequenceEvent<TransUnitEditEventHandler> implements HasTransUnitEditData{

	private final TransUnitId transUnitId;
	private final DocumentId documentId;
	private final String sessionId;
	
	/**
	 * Handler type.
	 */
	private static Type<TransUnitEditEventHandler> TYPE;

	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<TransUnitEditEventHandler> getType() {
		if (TYPE == null) {
			TYPE = new Type<TransUnitEditEventHandler>();
		}
		return TYPE;
	}
	
	public TransUnitEditEvent(HasTransUnitEditData data, int sequence) {
		super(sequence);
		this.documentId = data.getDocumentId();
		this.transUnitId = data.getTransUnitId();
		this.sessionId = data.getSessionId();
	}
	
	@Override
	protected void dispatch(TransUnitEditEventHandler handler) {
		handler.onTransUnitEdit(this);
	}

	@Override
	public Type<TransUnitEditEventHandler> getAssociatedType() {
		return getType();
	}

	@Override
	public DocumentId getDocumentId() {
		return documentId;
	}
	
	@Override
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return sessionId;
	}
	
}
