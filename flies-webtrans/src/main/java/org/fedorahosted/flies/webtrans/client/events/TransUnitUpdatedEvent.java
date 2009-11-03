package org.fedorahosted.flies.webtrans.client.events;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.HasTransUnitUpdatedData;
import org.fedorahosted.flies.gwt.rpc.TransUnitStatus;
import org.fedorahosted.flies.gwt.rpc.TransUnitUpdated;

import com.google.gwt.event.shared.GwtEvent;

public class TransUnitUpdatedEvent extends SequenceEvent<TransUnitUpdatedEventHandler> implements HasTransUnitUpdatedData{

	private final TransUnitId transUnitId;
	private final DocumentId documentId;
	private final TransUnitStatus previousStatus;
	private final TransUnitStatus newStatus;
	
	/**
	 * Handler type.
	 */
	private static Type<TransUnitUpdatedEventHandler> TYPE;

	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<TransUnitUpdatedEventHandler> getType() {
		if (TYPE == null) {
			TYPE = new Type<TransUnitUpdatedEventHandler>();
		}
		return TYPE;
	}
	
	public TransUnitUpdatedEvent(HasTransUnitUpdatedData data, int sequence) {
		super(sequence);
		this.documentId = data.getDocumentId();
		this.newStatus = data.getNewStatus();
		this.previousStatus = data.getPreviousStatus();
		this.transUnitId = data.getTransUnitId();
	}
	
	@Override
	protected void dispatch(TransUnitUpdatedEventHandler handler) {
		handler.onTransUnitUpdated(this);
	}

	@Override
	public Type<TransUnitUpdatedEventHandler> getAssociatedType() {
		return getType();
	}

	@Override
	public DocumentId getDocumentId() {
		return documentId;
	}

	@Override
	public TransUnitStatus getNewStatus() {
		return newStatus;
	};
	
	@Override
	public TransUnitStatus getPreviousStatus() {
		return previousStatus;
	}
	
	@Override
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
	
}
